package logback

import java.nio.charset.Charset

import ch.qos.logback.core.encoder.{Encoder, LayoutWrappingEncoder}
import ch.qos.logback.core.{ConsoleAppender => LConsoleAppender}

class ConsoleAppender[E] extends LConsoleAppender[E] {

  override def setEncoder(encoder: Encoder[E]): Unit = {
    encoder.asInstanceOf[LayoutWrappingEncoder[E]].setCharset(Charset.defaultCharset)
    this.encoder = encoder
  }

}
