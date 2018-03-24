package utils

import java.nio.charset.Charset
import java.nio.file.{ Files, Path, Paths }
import java.text.DecimalFormat
import javax.inject.{ Inject, Singleton }

import akka.stream.scaladsl.{ FileIO, Source }
import akka.util.ByteString
import org.slf4j.LoggerFactory
import play.api.libs.ws.{ BodyWritable, SourceBody, WSClient }
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.{ DataPart, FilePart }
import play.core.formatters.Multipart

import scala.concurrent.Future
import scala.util.Failure

@Singleton
class EncoderInfoSend @Inject() (
  ws: WSClient,
  hentaiConfig: HentaiConfig)(implicit defaultExecutionContext: scala.concurrent.ExecutionContext) {

  //val wsClientConfig: WSClientConfig = WSClientConfig(connectionTimeout = Duration.Inf)
  //val ws1 = NingWSClient(NingWSClientConfig(wsClientConfig = wsClientConfig))

  val logger = LoggerFactory.getLogger(classOf[EncoderInfoSend])
  //implicit val _ec = defaultExecutionContext
  def uploadVideo(fileStr: Path): Future[String] = Future {

    val path = Paths.get(hentaiConfig.rootPath)
    //val parentFile = Paths.get(path, fileStr)
    val sourceFile = fileStr
    //val parentUrl = parentFile.toURI.toString
    //val currentUrl = new URI(parentUrl + fileStr)
    //val sourceFile = new File(currentUrl)
    val key = s"里番-${sourceFile.getFileName}"

    val fileExists = Files.exists(sourceFile)

    logger.info(
      s"""开始发送里番文件
         |文件名:${sourceFile.getFileName}
         |文件是否存在于文件系统:${if (fileExists) "是" else "否"}""".stripMargin)

    val fileSize = Files.size(sourceFile)
    val decimalFormat = new DecimalFormat(",###")
    val fileFormattedSize = decimalFormat.format(fileSize)

    /*import org.apache.http.client.methods.CloseableHttpResponse
    import org.apache.http.client.methods.HttpPost
    import org.apache.http.util.CharsetUtils

    import org.apache.http.client.config.AuthSchemes
    import org.apache.http.client.config.CookieSpecs
    import org.apache.http.client.config.RequestConfig
    import org.apache.http.client.config.RequestConfig.Builder
    import org.apache.http.config.Registry
    import org.apache.http.config.RegistryBuilder
    import org.apache.http.conn.socket.ConnectionSocketFactory
    import org.apache.http.conn.socket.PlainConnectionSocketFactory
    import org.apache.http.impl.client.CloseableHttpClient
    import org.apache.http.impl.client.HttpClientBuilder
    import org.apache.http.impl.client.HttpClients
    import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
    import org.apache.http.entity.mime.content.FileBody
    import java.util
    Future {
      def createHttpClient(socketTimeout: Int): CloseableHttpClient = {
        val builder = RequestConfig.custom
        builder.setConnectTimeout(5000) // 设置连接超时时间，单位毫秒

        builder.setConnectionRequestTimeout(1000) // 设置从connect Manager获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。

        if (socketTimeout >= 0) builder.setSocketTimeout(socketTimeout) // 请求获取数据的超时时间，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
        val defaultRequestConfig = builder
          .setCookieSpec(CookieSpecs.STANDARD_STRICT)
          .setExpectContinueEnabled(true)
          .setTargetPreferredAuthSchemes(util.Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
          .setProxyPreferredAuthSchemes(util.Arrays.asList(AuthSchemes.BASIC)).build
        // 开启HTTPS支持
        //enableSSL
        // 创建可用Scheme
        val socketFactoryRegistry = RegistryBuilder.create[ConnectionSocketFactory].register("http", PlainConnectionSocketFactory.INSTANCE).build
        // 创建ConnectionManager，添加Connection配置信息
        val connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry)
        val httpClientBuilder = HttpClients.custom
        //if (retryTimes > 0) setRetryHandler(httpClientBuilder, retryTimes)
        val httpClient = httpClientBuilder.setConnectionManager(connectionManager).setDefaultRequestConfig(defaultRequestConfig).build
        httpClient
      }

      var httpClient: CloseableHttpClient = null
      try {
        if (httpClient == null) {
          httpClient = createHttpClient(3000 * 1000)
        }
        val contentType = ContentType.create("text/plain", "utf-8")
        // 把文件转换成流对象FileBody
        val fileBody = new FileBody(sourceFile.toFile, contentType, sourceFile.getFileName.toString)
        // 以浏览器兼容模式运行，防止文件名乱码。
        val reqEntity = MultipartEntityBuilder.create.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
          .addPart("video_0", fileBody)
          .addTextBody("videoKey", key, contentType)
          .addTextBody("videoInfo", sourceFile.toUri.toString.drop(path.toUri.toString.size), contentType)
          .addTextBody("returnPath", hentaiConfig.selfUrl, contentType)
          .addTextBody("encodeType", "ffmpegEncoder", contentType)
          .addTextBody("videoLength", 1.toString, contentType)
          .setCharset(CharsetUtils.get("UTF-8"))
          .build
        // uploadFile对应服务端类的同名属性<File类型>
        // .addPart("uploadFileName", uploadFileName)
        // uploadFileName对应服务端类的同名属性<String类型>
        val httpPost = new HttpPost(hentaiConfig.encoderUrl)
        httpPost.setEntity(reqEntity)
        val httpResponse = httpClient.execute(httpPost)
        val body = EntityUtils.toString(httpResponse.getEntity, "utf-8")
        val statusCode = httpResponse.getStatusLine.getStatusCode
        body -> statusCode
        /*val statusCode = httpResponse.getStatusLine.getStatusCode
        val content = getResult(httpResponse, charset)
        return new Nothing(statusCode, content)*/
      } finally {
        if (httpClient != null) try
          httpClient.close()
        catch {
          case e: Exception =>

        }
        if (httpClient != null) try
          httpClient.close
        catch {
          case e: Exception =>
        }
      }
    }.map {
      case (body, statusCode) =>
      val resultModel = if (statusCode == 200) {
        logger.info(
          s"""上传文件成功
             |文件名:${sourceFile.getFileName}
             |文件路径:${sourceFile}
             |文件大小:${fileFormattedSize}字节
             """.stripMargin
        )
        body
      } else {
        val errorStr =
          s"""上传文件返回异常代码:${statusCode}
             |文件路径:${sourceFile}
             |错误内容:\n${body}
             |文件大小:${fileFormattedSize}字节""".stripMargin
        val errorStr1 =
          s"""上传文件返回异常代码:${statusCode}
             |文件路径:${sourceFile}
             |文件大小:${fileFormattedSize}字节""".stripMargin
        logger.error(errorStr1)
        body
      }
      resultModel
    }.andThen {
      case Failure(e) =>
        logger.error(s"""上传文件失败
                        |文件名:${sourceFile.getFileName}
                        |文件路径:${sourceFile}
                        |文件大小:${fileFormattedSize}字节""".stripMargin, e)
    }*/

    //val aa = new String(encode(Charset.forName("gbk"), sourceFile.getFileName.toString).toByteArray, "gbk")
    //【钢琴_郎朗】郎朗_视奏《前前前世》
    //\??????_?????????_?????????????????_RADWIMPS?
    //val aa = new String(encode(Charset.forName("utf-8"), sourceFile.getFileName.toString).toByteArray, "gbk")
    //銆愰挗鐞確閮庢湕銆戦儙鏈梍瑙嗗銆婂墠鍓嶅墠涓栥?嬮挗鐞存洸_RADWIMPS銆愪綘
    //ADWIMPS【你的名字??????更换无水印源???_演奏_音乐_bilibili_哔哩哔哩
    //val aa = new String(encode(Charset.forName("utf-8"), sourceFile.getFileName.toString).toByteArray)
    //銆愰挗鐞確閮庢湕銆戦儙鏈梍瑙嗗銆婂墠鍓嶅墠涓栥?嬮挗鐞存洸_RADWIMPS銆愪綘
    //ADWIMPS【你的名字??????更换无水印源???_演奏_音乐_bilibili_哔哩哔哩  println("11" * 100)
    //val aa = new String(encode(Charset.forName("utf-8"), sourceFile.getFileName.toString).toByteArray, "utf-8")
    //【钢琴_郎朗】郎朗_视奏《前前前世》钢琴曲_RADWIMPS【你的名字】【更换无水印源】_演
    //\??????_?????????_?????????????????_RADWIMPS?
    //val aa = new String(encode(Charset.forName("utf-8"), sourceFile.getFileName.toString).toByteArray, "iso8859-1")
    //??é?????_é????????é?????_è§??????????????????????é????????_RADWIMP
    //??é?????_é????????é?????_è§??????????????????????é????????_RADWIMP
    //val aa = new String(encode(Charset.forName("iso8859-1"), sourceFile.getFileName.toString).toByteArray, "gbk")
    //???_?????_???????????_RADWIMPS??????????????_??_??_bilibil
    //???_?????_???????????_RADWIMPS??????????????_??_??_bilibil

    //(ws.url(hentaiConfig.encoderUrl): StandaloneWSRequest)
    ws.url(hentaiConfig.encoderUrl)
      .addHttpHeaders()
      .post(
        Source(
          FilePart("video_0", new String(sourceFile.getFileName.toString.getBytes("utf-8"), "gbk"), Option("""multipart/form-data; charset=UTF-8"""), FileIO.fromPath(sourceFile)) ::
            DataPart("videoKey", key) ::
            DataPart("videoInfo", sourceFile.toUri.toString.drop(path.toUri.toString.size)) ::
            DataPart("returnPath", hentaiConfig.selfUrl) ::
            //DataPart("encodeType", "FormatFactoryEncoder") ::
            DataPart("encodeType", "ffmpegEncoder") ::
            DataPart("videoLength", 1.toString) ::
            Nil))
      .map { wsResult =>
        val resultModel = if (wsResult.status == 200) {
          logger.info(
            s"""上传文件成功
               |文件名:${sourceFile.getFileName}
               |文件路径:${sourceFile}
               |文件大小:${fileFormattedSize}字节
             """.stripMargin)
          wsResult.body
        } else {
          val errorStr =
            s"""上传文件返回异常代码:${wsResult.status}
               |文件路径:${sourceFile}
               |错误内容:\n${wsResult.body}
               |文件大小:${fileFormattedSize}字节""".stripMargin
          val errorStr1 =
            s"""上传文件返回异常代码:${wsResult.status}
               |文件路径:${sourceFile}
               |文件大小:${fileFormattedSize}字节""".stripMargin
          logger.error(errorStr1)
          wsResult.body
        }
        resultModel
      }.andThen {
        case Failure(e) =>
          logger.error(s"""上传文件失败
                          |文件名:${sourceFile.getFileName}
                          |文件路径:${sourceFile}
                          |文件大小:${fileFormattedSize}字节""".stripMargin, e)
      }
  }.flatMap(identity)

  @deprecated("已不再使用，目前使用 javascript 实现的前端字幕代替", "0.0.1")
  def uploadVideoWithAss(videoFile: Path, assFile: Path): Future[String] = Future {
    val path = hentaiConfig.rootPath
    val parentFile = Paths.get(path)
    val parentUrl = parentFile.toUri.toString
    val videoPath = videoFile
    val assPath = assFile
    val videoPathExists = Files.exists(videoPath)
    val assPathExists = Files.exists(assPath)

    val key = s"里番-${videoPath.getFileName}"

    val decimalFormat = new DecimalFormat(",###")
    val videoFileSize = Files.size(videoPath)
    val videoFileFormattedSize = decimalFormat.format(videoFileSize)
    Charset.defaultCharset()

    val assFileSize = Files.size(assPath)
    val assFileFormattedSize = decimalFormat.format(assFileSize)

    logger.info(
      s"""开始发送里番文件
         |视频文件名:${videoPath.getFileName}
         |视频文件是否存在于文件系统:${if (videoPathExists) "是" else "否"}
         |字幕文件名:${assPath.getFileName}
         |字幕文件是否存在于文件系统:${if (assPathExists) "是" else "否"}""".stripMargin)

    ws.url(hentaiConfig.encoderUrl)
      .post(
        Source(
          FilePart("video_0", videoPath.getFileName.toString, Option("text/plain"), FileIO.fromPath(videoPath)) ::
            FilePart("video_1", assPath.getFileName.toString, Option("text/plain"), FileIO.fromPath(assPath)) ::
            DataPart("videoKey", key) ::
            DataPart("videoInfo", videoPath.toUri.toString.drop(parentUrl.size)) ::
            DataPart("returnPath", hentaiConfig.selfUrl) ::
            //DataPart("encodeType", "FormatFactoryEncoder") ::
            DataPart("encodeType", "ffmpegEncoderWithAss") ::
            DataPart("videoLength", 2.toString) ::
            Nil))
      .map { wsResult =>
        val resultModel = if (wsResult.status == 200) {
          //RequestInfo(true, wsResult.body)
          logger.info(
            s"""上传文件成功
               |视频文件名:${videoPath.getFileName}
               |字幕文件名:${assPath.getFileName}
               |视频文件路径:${videoPath}
               |字幕文件路径:${assPath}
               |视频文件大小:${videoFileFormattedSize}字节
               |字幕文件大小:${assFileFormattedSize}字节
              """.stripMargin)
          wsResult.body
        } else {
          val errorStr =
            s"""上传文件返回异常代码:${wsResult.status}
               |视频文件名:${videoPath.getFileName}
               |字幕文件名:${assPath.getFileName}
               |视频文件路径:${videoPath}
               |字幕文件路径:${assPath}
               |视频文件大小:${videoFileFormattedSize}字节
               |字幕文件大小:${assFileFormattedSize}字节
               |错误内容:\n${wsResult.body}""".stripMargin
          //RequestInfo(false, errorStr)
          logger.error(errorStr)
          wsResult.body
        }
        resultModel
      }.andThen {
        case Failure(e) =>
          logger.error(s"""上传文件失败
                        |视频文件名:${videoPath.getFileName}
                        |字幕文件名:${assPath.getFileName}
                        |视频文件路径:${videoPath}
                        |字幕文件路径:${assPath}
                        |视频文件大小:${videoFileFormattedSize}字节
                        |字幕文件大小:${assFileFormattedSize}字节""".stripMargin, e)
      }
  }.flatMap(identity)

}