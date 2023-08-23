object OperatingModeEnum extends Enumeration {
    val AUTOMATIC, SEMIAUTOMATIC, MANUAL, SERVICE, TEACHIN = Value
}

/**
 * all encompassing state of the AGV.
 *
 * @param headerId headerId of the message. The headerId is defined per topic and incremented by 1 with each sent (but not necessarily received) message.
 * @param timestamp Timestamp in ISO8601 format (YYYY-MM-DDTHH:mm:ss.ssZ).
 * @param version Version of the protocol [Major].[Minor].[Patch]
 * @param manufacturer Manufacturer of the AGV
 * @param serialNumber Serial number of the AGV.
 * @param orderId Unique order identification of the current order or the previous finished order. The orderId is kept until a new order is received. Empty string ("") if no previous orderId is available.
 * @param orderUpdateId Order Update Identification to identify that an order update has been accepted by the AGV. "0" if no previous orderUpdateId is available.
 * @param zoneSetId Unique ID of the zone set that the AGV currently uses for path planning. Must be the same as the one used in the order, otherwise the AGV is to reject the order.
 *                  Optional: If the AGV does not use zones, this field can be omitted.
 * @param lastNodeId nodeID of last reached node or, if AGV is currently on a node, current node (e.g., "node7"). Empty string ("") if no lastNodeId is available.
 * @param lastNodeSequenceId sequenceId of the last reached node or, if the AGV is currently on a node, sequenceId of current node.
 *                           â€œ0â€ if no lastNodeSequenceId is available.
 * @param driving True: indicates that the AGV is driving and/or rotating. Other movements of the AGV (e.g., lift movements) are not included here.
 *                False: indicates that the AGV is neither driving nor rotating
 * @param paused True: AGV is currently in a paused state, either because of the push of a physical button on the AGV or because of an instantAction. The AGV can resume the order.
 *               False: The AGV is currently not in a paused state.
 * @param newBaseRequest True: AGV is almost at the end of the base and will reduce speed if no new base is transmitted. Trigger for master control to send new base
 *                       False: no base update required.
 * @param distanceSinceLastNode Used by line guided vehicles to indicate the distance it has been driving past the "lastNodeId".
 *                              Distance is in meters.
 * @param operatingMode Current operating mode of the AGV.
 * @param nodeStates Array of nodeState-Objects, that need to be traversed for fulfilling the order. Empty list if idle.
 * @param edgeStates Array of edgeState-Objects, that need to be traversed for fulfilling the order, empty list if idle.
 * @param agvPosition Defines the position on a map in world coordinates. Each floor has its own map.
 * @param velocity The AGVs velocity in vehicle coordinates
 * @param loads Loads, that are currently handled by the AGV. Optional: If AGV cannot determine load state, leave the array out of the state. If the AGV can determine the load state, but the array is empty, the AGV is considered unloaded.
 * @param actionStates Contains a list of the current actions and the actions which are yet to be finished. This may include actions from previous nodes that are still in progress
 *                     When an action is completed, an updated state message is published with actionStatus set to finished and if applicable with the corresponding resultDescription. The actionStates are kept until a new order is received.
 * @param batteryState Contains all battery-related information.
 * @param errors Array of error-objects. All active errors of the AGV should be in the list. An empty array indicates that the AGV has no active errors.
 * @param information Array of info-objects. An empty array indicates, that the AGV has no information. This should only be used for visualization or debugging – it must not be used for logic in master control.
 * @param safetyState Contains all safety-related information.
 */
case class State (
     headerId: Int,
     timestamp: String,
     version: String,
     manufacturer: String,
     serialNumber: String,
     orderId: String,
     orderUpdateId: Int,
     zoneSetId: Option[String],
     lastNodeId: String,
     lastNodeSequenceId: Int,
     driving: Boolean,
     paused: Option[Boolean],
     newBaseRequest: Option[Boolean],
     distanceSinceLastNode: Option[Double],
     operatingMode: OperatingModeEnum.Value,
     nodeStates: List[NodeState],
     edgeStates: List[EdgeStates],
     agvPosition: Option[AgvPosition],
     velocity: Option[Velocity],
     loads: Option[List[Load]],
     actionStates: List[ActionState],
     batteryState: BatteryState,
     errors: List[Error],
     information: Option[List[Information]],
     safetyState: SafetyState
)


/**
 * @param nodeId Unique node identification
 * @param sequenceId sequenceId to discern multiple nodes with same nodeId.
 * @param nodeDescription Additional information on the node.
 * @param nodePosition Node position. The object is defined in chapter 5.4 Topic: Order (from master control to AGV).
 *                     Optional:Master control has this information. Can be sent additionally, e.g., for debugging purposes.
 * @param released True: indicates that the node is part of the base. False: indicates that the node is part of the horizon.
 */
case class NodeState (
     nodeId: String,
     sequenceId: Int,
     nodeDescription: Option[String],
     nodePosition: Option[NodePosition],
     released: Boolean
)


/**
 * Node position. The object is defined in chapter 5.4 Topic: Order (from master control to AGV).
 * Optional:Master control has this information. Can be sent additionally, e.g., for debugging purposes. 
 *
 * @param x
 * @param y
 * @param theta
 * @param mapId
 */
case class NodePosition (
     x: Double,
     y: Double,
     theta: Double,
     mapId: String
)


/**
 * @param edgeId Unique edge identification
 * @param sequenceId sequenceId of the edge.
 * @param edgeDescription Additional information on the edge.
 * @param released True indicates that the edge is part of the base. False indicates that the edge is part of the horizon.
 * @param trajectory The trajectory is to be communicated as a NURBS and is defined in chapter 6.7 Implementation of the Order message.
 *                   Trajectory segments reach from the point, where the AGV starts to enter the edge to the point where it reports that the next node was traversed.
 */
case class EdgeStates (
     edgeId: String,
     sequenceId: Int,
     edgeDescription: Option[String],
     released: Boolean,
     trajectory: Option[Trajectory]
)


/**
 * The trajectory is to be communicated as a NURBS and is defined in chapter 6.7 Implementation of the Order message.
 * Trajectory segments reach from the point, where the AGV starts to enter the edge to the point where it reports that the next node was traversed. 
 *
 * @param degree Defines the number of control points that influence any given point on the curve. Increasing the degree increases continuity. If not defined, the default value is 1.
 * @param knotVector Sequence of parameter values that determine where and how the control points affect the NURBS curve. knotVector has size of number of control points + degree + 1
 * @param controlPoints List of JSON controlPoint objects defining the control points of the NURBS, which includes the beginning and end point.
 */
case class Trajectory (
     degree: Int,
     knotVector: List[Double],
     controlPoints: List[ControlPoints]
)


/**
 * @param x
 * @param y
 * @param weight The weight, with which this control point pulls on the curve.
 *               When not defined, the default will be 1.0.
 */
case class ControlPoints (
     x: Double,
     y: Double,
     weight: Option[Double]
)


/**
 * Defines the position on a map in world coordinates. Each floor has its own map.
 *
 * @param x
 * @param y
 * @param theta
 * @param mapId
 * @param mapDescription
 * @param positionInitialized True: position is initialized. False: position is not initizalized.
 * @param localizationScore Describes the quality of the localization and therefore, can be used, e.g., by SLAM-AGV to describe how accurate the current position information is.
 *                          0.0: position unknown
 *                          1.0: position known
 *                          Optional for vehicles that cannot estimate their localization score.
 *                          Only for logging and visualization purposes
 * @param deviationRange Value for position deviation range in meters. Optional for vehicles that cannot estimate their deviation, e.g., grid-based localization. Only for logging and visualization purposes.
 */
case class AgvPosition (
     x: Double,
     y: Double,
     theta: Double,
     mapId: String,
     mapDescription: Option[String],
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
 * @param vx The AVGs velocity in its x direction
 * @param vy The AVGs velocity in its y direction
 * @param omega The AVGs turning speed around its z axis.
 */
case class Velocity (
     vx: Option[Double],
     vy: Option[Double],
     omega: Option[Double]
)


/**
 * Load object that describes the load if the AGV has information about it.
 *
 * @param loadId Unique identification number of the load (e.g., barcode or RFID). Empty field, if the AGV can identify the load, but did not identify the load yet. Optional, if the AGV cannot identify the load.
 * @param loadType Type of load.
 * @param loadPosition Indicates, which load handling/carrying unit of the AGV is used, e.g., in case the AGV has multiple spots/positions to carry loads. Optional for vehicles with only one loadPosition.
 * @param boundingBoxReference Point of reference for the location of the bounding box. The point of reference is always the center of the bounding box bottom surface (at height = 0) and is described in coordinates of the AGV coordinate system.
 * @param loadDimensions Dimensions of the loads bounding box in meters.
 * @param weight Absolute weight of the load measured in kg.
 */
case class Load (
     loadId: Option[String],
     loadType: Option[String],
     loadPosition: Option[String],
     boundingBoxReference: Option[BoundingBoxReference],
     loadDimensions: Option[LoadDimensions],
     weight: Option[Double]
){
    assert( weight.forall(_ >= 0), "`weight` must be greater than or equal to 0" )
}


/**
 * Point of reference for the location of the bounding box. The point of reference is always the center of the bounding box bottom surface (at height = 0) and is described in coordinates of the AGV coordinate system.
 *
 * @param x
 * @param y
 * @param z
 * @param theta Orientation of the loads bounding box. Important for tugger, trains, etc.
 */
case class BoundingBoxReference (
     x: Double,
     y: Double,
     z: Double,
     theta: Option[Double]
)


/**
 * Dimensions of the loads bounding box in meters.
 *
 * @param length Absolute length of the loads bounding box in meter.
 * @param width Absolute width of the loads bounding box in meter.
 * @param height Absolute height of the loads bounding box in meter.
 *               Optional:
 *               Set value only if known.
 */
case class LoadDimensions (
     length: Double,
     width: Double,
     height: Option[Double]
)

object ActionStatusEnum extends Enumeration {
    val WAITING, INITIALIZING, RUNNING, FINISHED, FAILED = Value
}

/**
 * @param actionId Unique actionId
 * @param actionType actionType of the action.
 *                   Optional: Only for informational or visualization purposes. Order knows the type.
 * @param actionDescription Additional information on the current action.
 * @param actionStatus WAITING: waiting for the trigger (passing the mode, entering the edge) PAUSED: paused by instantAction or external trigger FAILED: action could not be performed.
 * @param resultDescription Description of the result, e.g., the result of a RFID-read. Errors will be transmitted in errors.
 */
case class ActionState (
     actionId: String,
     actionType: Option[String],
     actionDescription: Option[String],
     actionStatus: ActionStatusEnum.Value,
     resultDescription: Option[String]
)


/**
 * Contains all battery-related information.
 *
 * @param batteryCharge State of Charge in %:
 *                      If AGV only provides values for good or bad battery levels, these will be indicated as 20% (bad) and 80% (good).
 * @param batteryVoltage Battery voltage
 * @param batteryHealth State of health in percent.
 * @param charging True: charging in progress. False: AGV is currently not charging.
 * @param reach Estimated reach with current State of Charge in meter.
 */
case class BatteryState (
     batteryCharge: Double,
     batteryVoltage: Option[Double],
     batteryHealth: Option[Double],
     charging: Boolean,
     reach: Option[Double]
){
    assert( batteryHealth.forall(_ >= 0), "`batteryHealth` must be greater than or equal to 0" )
    assert( batteryHealth.forall(_ <= 100), "`batteryHealth` must be less than or equal to 100" )
    assert( reach.forall(_ >= 0), "`reach` must be greater than or equal to 0" )
}

object ErrorLevelEnum extends Enumeration {
    val WARNING, FATAL = Value
}

/**
 * @param errorType Type/name of error.
 * @param errorReferences
 * @param errorDescription Error description.
 * @param errorLevel WARNING: AGV is ready to start (e.g., maintenance cycle expiration warning). FATAL: AGV is not in running condition, user intervention required (e.g., laser scanner is contaminated).
 */
case class Error (
     errorType: String,
     errorReferences: Option[List[ErrorReference]],
     errorDescription: Option[String],
     errorLevel: ErrorLevelEnum.Value
)


/**
 * Array of references to identify the source of the error (e.g., headerId, orderId, actionId, etc.).
 *
 * @param referenceKey References the type of reference (e.g., headerId, orderId, actionId, etc.).
 * @param referenceValue References the value, which belongs to the reference key.
 */
case class ErrorReference (
     referenceKey: String,
     referenceValue: String
)

object InfoLevelEnum extends Enumeration {
    val INFO, DEBUG = Value
}

/**
 * @param infoType Type/name of information.
 * @param infoReferences
 * @param infoDescription Info of description.
 * @param infoLevel DEBUG: used for debugging. INFO: used for visualization.
 */
case class Information (
     infoType: String,
     infoReferences: Option[List[InfoReference]],
     infoDescription: Option[String],
     infoLevel: InfoLevelEnum.Value
)


/**
 * Array of references.
 *
 * @param referenceKey References the type of reference (e.g., headerId, orderId, actionId, etc.).
 * @param referenceValue References the value, which belongs to the reference key.
 */
case class InfoReference (
     referenceKey: String,
     referenceValue: String
)

object EStopEnum extends Enumeration {
    val AUTOACK, MANUAL, REMOTE, NONE = Value
}

/**
 * Contains all safety-related information.
 *
 * @param eStop Acknowledge-Type of eStop: AUTOACK: auto-acknowledgeable e-stop is activated, e.g., by bumper or protective field. MANUAL: e-stop hast to be acknowledged manually at the vehicle. REMOTE: facility e-stop has to be acknowledged remotely. NONE: no e-stop activated.
 * @param fieldViolation Protective field violation. True: field is violated. False: field is not violated.
 */
case class SafetyState (
     eStop: EStopEnum.Value,
     fieldViolation: Boolean
)

