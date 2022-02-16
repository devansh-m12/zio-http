package zhttp.service.client.domain

import io.netty.channel.Channel
import io.netty.handler.codec.http.FullHttpRequest
import zhttp.service.client.domain.ConnectionData.ReqKey
import zio.Ref
import zio.duration.Duration

import java.net.InetSocketAddress
import java.time.Instant
import scala.collection.immutable

/**
 * Defines ClientData / Request Key and other types for managing connection data
 * TODO: Likely to change
 *
 * @param channel
 * @param isReuse
 */
case class Connection(channel: Channel, isReuse: Boolean) {
  override def canEqual(that: Any): Boolean = that match {
    case that: Connection => this.channel.id() == that.channel.id()
    case _                => false
  }
}

case class Timeouts(
  connectionTimeout: Duration = Duration.Infinity,
  idleTimeout: Duration = Duration.Infinity,
  requestTimeout: Duration = Duration.Infinity,
)

case class PendingRequest(req: FullHttpRequest, requestedTime: Instant)

case class ConnectionState(
  currentAllocatedChannels: Map[Channel, ReqKey],
  idleConnectionsMap: Map[ReqKey, immutable.Queue[Connection]],
)

case class ConnectionData(connectionData: Ref[(Option[Connection], ConnectionState)]) {

  def nextIdleChannel(reqKey: ReqKey) = ???

  def getIdleChannel(
    reqKey: ReqKey,
    currentAllocatedChannels: Map[Channel, ReqKey],
    idleConnectionsMap: Map[ReqKey, immutable.Queue[Connection]],
  ) = ???

  def setConnectionIdle(connection: Connection, reqKey: ReqKey): zio.Task[Unit] = ???

  def addIdleChannel(
    connection: Connection,
    reqKey: ReqKey,
    currentAllocatedChannels: Map[Channel, ReqKey],
    idleConnectionsMap: Map[ReqKey, immutable.Queue[Connection]],
  ) = ???

  def getTotalConnections = for {
    connectionData <- connectionData.get
    allocConnections = connectionData._2.currentAllocatedChannels.size
    idleConnections  = connectionData._2.idleConnectionsMap.valuesIterator
      .foldLeft(0) { case (acc, queue) => acc + queue.size }
  } yield (allocConnections + idleConnections)

  //    def incrementConnection: Unit = ???
  //    def decrementConnection = ???
  //    def isConnectionExpired = ???
  //    def isConnectionWithinLimits = ???
  //    def addConnectionToIdleQ = ???
  //    def addConnectionToWaitQ = ???
  //
  //    def releaseConnection = ???
  //    def shutdownConnectionManager = ???
  //
  //    def getActiveConnectionsForReqKey(reqKey: ReqKey): Task[Int] = ???
  //    def getIdleConnections: Task[Int] = ???
  //    def getIdleConnectionsForReqKey(reqKey: ReqKey): Task[Int] = ???

  // TBD thready safety and appropriate namespace
  var currMaxTotalConnections: Int     = 0
  var currMaxConnectionPerRequest: Int = 0
  var currMaxWaitingReq: Int           = 0

}

object ConnectionData {
  type ReqKey = InetSocketAddress
  def emptyIdleConnectionMap = Map.empty[ReqKey, immutable.Queue[Connection]]
}