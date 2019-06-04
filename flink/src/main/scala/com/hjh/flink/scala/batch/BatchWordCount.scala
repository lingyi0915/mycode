package com.hjh.flink.scala.batch

import javax.sql.DataSource
import org.apache.flink.api.scala.ExecutionEnvironment

/**
  * @Author: hjh
  * @Create: 2019/3/22
  * @Description:
  */
object BatchWordCount {
  def main(args: Array[String]): Unit = {
    val env = ExecutionEnvironment.getExecutionEnvironment
    val inputPath = "D:\\workspace\\测试数据\\pcac.access_2017-03-06.log"

    val outPath = "D:\\2"
    val text = env.readTextFile(inputPath)

    import org.apache.flink.api.scala._

    val count = text.flatMap(_.toLowerCase.split("\\W+"))
      .filter(_.nonEmpty)
      .map((_,1))
      .groupBy(0)
      .sum(1)
    count.writeAsText(outPath).setParallelism(1)
    env.execute("batch scala demo")
  }
}