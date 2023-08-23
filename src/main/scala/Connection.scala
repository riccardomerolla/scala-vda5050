object ConnectionStateEnum extends Enumeration {
    val ONLINE, OFFLINE, CONNECTIONBROKEN = Value
}

/**
 * The last will message of the AGV. Has to be sent with retain flag.
 * Once the AGV comes online, it has to send this message on its connect topic, with the connectionState enum set to "ONLINE".
 *  The last will message is to be configured with the connection state set to "CONNECTIONBROKEN".
 * Thus, if the AGV disconnects from the broker, master control gets notified via the topic "connection".
 * If the AGV is disconnecting in an orderly fashion (e.g. shutting down, sleeping), the AGV is to publish a message on this topic with the connectionState set to "DISCONNECTED".
 *
 * @param headerId Header ID of the message. The headerId is defined per topic and incremented by 1 with each sent (but not necessarily received) message.
 * @param timestamp Timestamp in ISO8601 format (YYYY-MM-DDTHH:mm:ss.ssZ).
 * @param version Version of the protocol [Major].[Minor].[Patch]
 * @param manufacturer Manufacturer of the AGV.
 * @param serialNumber Serial number of the AGV.
 * @param connectionState ONLINE: connection between AGV and broker is active. OFFLINE: connection between AGV and broker has gone offline in a coordinated way. CONNECTIONBROKEN: The connection between AGV and broker has unexpectedly ended.
 */
case class Connection (
     headerId: Int,
     timestamp: String,
     version: String,
     manufacturer: String,
     serialNumber: String,
     connectionState: ConnectionStateEnum.Value
)

