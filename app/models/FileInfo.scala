package models

case class FileInfo(
                   fileName: String,
                   requestUrl: play.api.mvc.Call,
                   temfileExists: Boolean,
                   canEncode: Boolean
                   )