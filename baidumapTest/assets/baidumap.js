$(function() {

    var bindOerlay = function(baiduMap, overlay, currentViewModel) {
        currentViewModel.overlays(overlay);

        var addPoints = function() {
            var paths = overlay.getPath();
            var pointModel = [];
            var length = paths.length;
            for (var i = 0; i < length; i++) {
                pointModel.push({ point: paths[i], iconOverlay: cureateNingshi(paths[i]) });
            }

            currentViewModel.points(pointModel);
        };

        overlay.addEventListener("lineupdate", function() {
            addPoints();
        });
        addPoints();

        clearOhterOverlays(baiduMap, overlay);
        console.log(viewModel.points());
    };

    var styleOptions = {
        strokeColor:"red",    //边线颜色。
        fillColor:"red",      //填充颜色。当参数为空时，圆形将没有填充效果。
        strokeWeight: 3,       //边线的宽度，以像素为单位。
        strokeOpacity: 0.8,	   //边线透明度，取值范围0 - 1。
        fillOpacity: 0.6,      //填充的透明度，取值范围0 - 1。
        strokeStyle: 'solid' //边线的样式，solid或dashed。
    };

    var ViewModel = function(baiduMap) {
        var self = this;

        self.state = ko.observable("common");

        self.overlays = ko.observable(null);
        self.points = ko.observableArray([]);
        self.currentCSV = ko.observable("");
        self.singleRowCSV = ko.observable("");

        self.lightPoint = function(pointModel) {
            baiduMap.addOverlay(pointModel.iconOverlay);              // 将标注添加到地图中
        };
        self.removeLight = function(pointModel) {
            baiduMap.removeOverlay(pointModel.iconOverlay);              // 将标注添加到地图中
        };

        self.clearAllOverlays = function() {
            for(var i = 0; i < self.overlays().length; i++) {
                baiduMap.removeOverlay(self.overlays()[i]);
            }
            self.overlays([]);
        };

        self.enableEditing = function() {
            self.overlays().enableEditing();
        };

        self.disableEditing = function() {
            self.overlays().disableEditing();
        };

        self.removePoint = function(pointModel) {
            baiduMap.removeOverlay(pointModel.iconOverlay);              // 将标注添加到地图中
            var newPoints = [];
            //var newPointModels = [];
            for (var i = 0; i < self.points().length; i++) {
                var dealPoint = self.points()[i];
                if (pointModel === dealPoint) {
                } else {
                    newPoints.push(dealPoint.point);
                    //newPointModels.push(pointModel);
                }
            }
            console.log(newPoints);
            self.overlays().setPath(newPoints);
        };

        self.toCSV = function() {
            if (typeof self.points() === "object" && (self.points() !== null)) {
                var str = "";
                var selfRow = "";
                for (var i = 0; i < self.points().length; i++) {
                    var currentPoint = self.points()[i];
                    str += currentPoint.point.lng;
                    str += ",";
                    str += currentPoint.point.lat;
                    str += ",";
                    str += "\n";

                    selfRow += currentPoint.point.lng;
                    selfRow += ",";
                    selfRow += currentPoint.point.lat;
                    selfRow += ",";
                }
                self.currentCSV(str);
                self.singleRowCSV(selfRow);
            }
            self.state("csv");
        };

        self.overlayFromPoints = function(points) {
            var polyline = new BMap.Polygon(points, styleOptions);   //创建折线
            baiduMap.addOverlay(polyline);   //增加折线
            var sumX = 0;
            var sumY = 0;
            for (var i = 0; i < points.length; i++) {
                var eachPoint = points[i];
                sumX += eachPoint.lng;
                sumY += eachPoint.lat;
            }
            var bPoint = new BMap.Point(sumX / points.length, sumY / points.length);
            baiduMap.centerAndZoom(bPoint, 16);
            bindOerlay(baiduMap, polyline, self);
        };

        self.overlayFromCSV = function() {
            self.state("common");
            var csvStr = self.currentCSV();
            var lines = csvStr.split(",\n");
            var points = [];
            for (var i = 0; i < lines.length; i++) {
                var eachLine = lines[i];
                var pointInfos = eachLine.split(",");
                var jingdu = parseFloat(pointInfos[0]);
                var weidu = parseFloat(pointInfos[1]);
                if (jingdu > 0 && weidu > 0) {
                    points.push(new BMap.Point(jingdu, weidu));
                }
            }

            setTimeout(function() {
                self.overlayFromPoints(points);
            }, 200);
        };

        self.overlayFromSingleLine = function() {
            self.state("common");
            var singleLineData = self.singleRowCSV();
            var data = singleLineData.split(",");
            var points = [];
            for (var i = 0; i < data.length - 1; i += 2) {
                var jingdu = parseFloat(data[i]);
                var weidu = parseFloat(data[i + 1]);
                if (jingdu > 0 && weidu > 0) {
                    points.push(new BMap.Point(jingdu, weidu));
                }
            }
            setTimeout(function() {
                self.overlayFromPoints(points);
            }, 200);
        };
    };

// 百度地图API功能
    var map = new BMap.Map("map");
    var poi = new BMap.Point(116.307852, 40.057031);

    var viewModel = new ViewModel(map);
    ko.applyBindings(viewModel);

    map.centerAndZoom(poi, 16);
    map.enableScrollWheelZoom();

    //清除除了目标以外的覆盖物
    function clearOhterOverlays(baiduMap, target) {
        var overlayToRemove = [];
        for (var i = 0; i < baiduMap.getOverlays().length; i++) {
            var current = baiduMap.getOverlays()[i];
            if (target === current) {
            } else {
                overlayToRemove.push(baiduMap.getOverlays()[i]);
            }
        }
        for (var i = 0; i < overlayToRemove.length; i++) {
            baiduMap.removeOverlay(overlayToRemove[i]);
        }
    };

    var cureateNingshi = function(targetPoint) {
        var myIcon = new BMap.Icon("./assets/images/pic-01.gif", new BMap.Size(21, 21));
        var marker2 = new BMap.Marker(targetPoint, { icon: myIcon });  // 创建标注
        return marker2;
    };

    var overlaycomplete = function(e) {
        bindOerlay(map, e.overlay, viewModel);
    };

//实例化鼠标绘制工具
    var drawingManager = new BMapLib.DrawingManager(map, {
        isOpen: false, //是否开启绘制模式
        enableDrawingTool: true, //是否显示工具栏
        drawingToolOptions: {
            anchor: BMAP_ANCHOR_TOP_RIGHT, //位置
            offset: new BMap.Size(5, 5), //偏离值
            drawingModes: [
                BMAP_DRAWING_POLYGON
            ]
        },
        //circleOptions: styleOptions, //圆的样式
        //polylineOptions: styleOptions, //线的样式
        polygonOptions: styleOptions //多边形的样式
        //rectangleOptions: styleOptions //矩形的样式
    });
//添加鼠标绘制工具监听事件，用于获取绘制结果
    drawingManager.addEventListener("overlaycomplete", overlaycomplete);
});