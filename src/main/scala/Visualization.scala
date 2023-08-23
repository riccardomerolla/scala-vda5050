
/**
 * AGV position and/or velocity for visualization purposes. Can be published at a higher rate if wanted. Since bandwidth may be expensive depening on the update rate for this topic, all fields are optional.
 *
 * @param headerId headerId of the message. The headerId is defined per topic and incremented by 1 with each sent (but not necessarily received) message.
 * @param timestamp Timestamp in ISO8601 format (YYYY-MM-DDTHH:mm:ss.ssZ).
 * @param version Version of the protocol [Major].[Minor].[Patch]
 * @param manufacturer Manufacturer of the AGV
 * @param serialNumber Serial number of the AGV.
 * @param agvPosition The AGVs position
 * @param velocity The AGVs velocity in vehicle coordinates
 */
case class Visualization (
     headerId: Option[Int],
     timestamp: Option[String],
     version: Option[String],
     manufacturer: Option[String],
     serialNumber: Option[String],
     agvPosition: Option[AgvPosition],
     velocity: Option[Velocity]
)


/**
 * The AGVs position
 *
 * @param x
 * @param y
 * @param theta
 * @param mapId
 * @param positionInitialized True if the AGVs position is initialized, false, if position is not initizalized.
 * @param localizationScore Localization score for SLAM based vehicles, if the AGV can communicate it.
 * @param deviationRange Value for position deviation range in meters. Can be used if the AGV is able to derive it.
 */
case class AgvPosition (
     x: Double,
     y: Double,
     theta: Double,
     mapId: String,
     positionInitialized: Boolean,
     localizationScore: Option[Double],
     deviationRange: Option[Double]
){
    assert( localizationScore.forall(_ >= 0), "`localizationScore` must be greater than or equal to 0" )
    assert( localizationScore.forall(_ <= 1), "`localizationScore` must be less than or equal to 1" )
}


/**
 * The AGVs velocity in vehicle coordinates
 *
 * @param vx
 * @param vy
 * @param omega
 */
case class Velocity (
     vx: Option[Double],
     vy: Option[Double],
     omega: Option[Double]
)

