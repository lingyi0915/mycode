package com.hjh.flink.scala.stream

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time

/**
  * @Author: hjh
  * @Create: 2019/3/22
  * @Description:
  */
object SocketWindowWordCount {
  def main(args: Array[String]): Unit = {

    val port:Int = try {
      ParameterTool.fromArgs(args).getInt("port")
    } catch {
      case e:Exception => {
        System.err.println("no port set. use default port 9000")
      }
        9000
    }
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val text = env.socketTextStream("hadoop",9000,'\n')

    import org.apache.flink.api.scala._

    val windowCounts = text.flatMap(line => line.split("\\s"))
      .map( w => WordWithCount(w,1))
      .keyBy("word")
      .timeWindow(Time.seconds(2),Time.seconds(1))
//      .sum("count")
      .reduce((a,b)=>WordWithCount(a.word,a.count+b.count))
    //设置并行度1
    windowCounts.print.setParallelism(1)
    //这句话开始执行
    env.execute("Socket Window WordCount")

  }
  case class WordWithCount(word: String,count:Long)
}
