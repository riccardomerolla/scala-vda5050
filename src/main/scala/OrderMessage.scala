
/**
 * The message schema to communicate orders from master control to the AGV.
 *
 * @param headerId headerId of the message. The headerId is defined per topic and incremented by 1 with each sent (but not necessarily received) message.
 * @param timestamp Timestamp in ISO8601 format (YYYY-MM-DDTHH:mm:ss.ssZ).
 * @param version Version of the protocol [Major].[Minor].[Patch]
 * @param manufacturer Manufacturer of the AGV
 * @param serialNumber Serial number of the AGV.
 * @param orderId Order Identification. This is to be used to identify multiple order messages that belong to the same order.
 * @param orderUpdateId orderUpdate identification. Is unique per orderId. If an order update is rejected, this field is to be passed in the rejection message.
 * @param zoneSetId Unique identifier of the zone set that the AGV has to use for navigation or that was used by MC for planning.
 *                  Optional: Some MC systems do not use zones. Some AGVs do not understand zones. Do not add to message if no zones are used.
 * @param nodes Array of nodes objects to be traversed for fulfilling the order. One node is enough for a valid order. Leave edge list empty for that case.
 * @param edges Directional connection between two nodes. Array of edge objects to be traversed for fulfilling the order. One node is enough for a valid order. Leave edge list empty for that case.
 */
case class OrderMessage (
     headerId: Int,
     timestamp: String,
     version: String,
     manufacturer: String,
     serialNumber: String,
     orderId: String,
     orderUpdateId: Int,
     zoneSetId: Option[String],
     nodes: List[Node],
     edges: List[Edge]
){
    assert( orderUpdateId >= 0, "`orderUpdateId` must be greater than or equal to 0" )
}


/**
 * @param nodeId Unique node identification
 * @param sequenceId Number to track the sequence of nodes and edges in an order and to simplify order updates.
 *                   The main purpose is to distinguish between a node which is passed more than once within one orderId. The variable sequenceId runs across all nodes and edges of the same order and is reset when a new orderId is issued.
 * @param nodeDescription Additional information on the node.
 * @param released True indicates that the node is part of the base. False indicates that the node is part of the horizon.
 * @param nodePosition Defines the position on a map in world coordinates. Each floor has its own map. All maps must use the same project specific global origin. 
 *                     Optional for vehicle-types that do not require the node position (e.g., line-guided vehicles).
 * @param actions Array of actions to be executed on a node. Empty array, if no actions required.
 */
case class Node (
     nodeId: String,
     sequenceId: Int,
     nodeDescription: Option[String],
     released: Boolean,
     nodePosition: Option[NodePosition],
     actions: List[Actions]
){
    assert( sequenceId >= 0, "`sequenceId` must be greater than or equal to 0" )
}


/**
 * Defines the position on a map in world coordinates. Each floor has its own map. All maps must use the same project specific global origin. 
 * Optional for vehicle-types that do not require the node position (e.g., line-guided vehicles).
 *
 * @param x X-position on the map in reference to the map coordinate system. Precision is up to the specific implementation.
 * @param y Y-position on the map in reference to the map coordinate system. Precision is up to the specific implementation.
 * @param theta Absolute orientation of the AGV on the node. 
 *              Optional: vehicle can plan the path by itself.
 *              If defined, the AGV has to assume the theta angle on this node. If previous edge disallows rotation, the AGV must rotate on the node. If following edge has a differing orientation defined but disallows rotation, the AGV is to rotate on the node to the edges desired rotation before entering the edge.
 * @param allowedDeviationXy Indicates how exact an AGV has to drive over a node in order for it to count as traversed.
 *                           If = 0: no deviation is allowed (no deviation means within the normal tolerance of the AGV manufacturer).
 *                           If > 0: allowed deviation-radius in meters. If the AGV passes a node within the deviation-radius, the node is considered to have been traversed.
 * @param allowedDeviationTheta Indicates how big the deviation of theta angle can be. 
 *                              The lowest acceptable angle is theta - allowedDeviationTheta and the highest acceptable angle is theta + allowedDeviationTheta.
 * @param mapId Unique identification of the map in which the position is referenced.
 *              Each map has the same origin of coordinates. When an AGV uses an elevator, e.g., leading from a departure floor to a target floor, it will disappear off the map of the departure floor and spawn in the related lift node on the map of the target floor.
 * @param mapDescription Additional information on the map.
 */
case class NodePosition (
     x: Double,
     y: Double,
     theta: Option[Double],
     allowedDeviationXy: Option[Double],
     allowedDeviationTheta: Option[Double],
     mapId: String,
     mapDescription: Option[String]
){
    assert( theta.forall(_ >= -3.14159265359), "`theta` must be greater than or equal to -3.14159265359" )
    assert( theta.forall(_ <= 3.14159265359), "`theta` must be less than or equal to 3.14159265359" )
    assert( allowedDeviationXy.forall(_ >= 0), "`allowedDeviationXy` must be greater than or equal to 0" )
    assert( allowedDeviationTheta.forall(_ >= -3.141592654), "`allowedDeviationTheta` must be greater than or equal to -3.141592654" )
    assert( allowedDeviationTheta.forall(_ <= 3.141592654), "`allowedDeviationTheta` must be less than or equal to 3.141592654" )
}

object BlockingTypeEnum extends Enumeration {
    val NONE, SOFT, HARD = Value
}

/**
 * Describes an action that the AGV can perform.
 *
 * @param actionType Name of action as described in the first column of "Actions and Parameters". Identifies the function of the action.
 * @param actionId Unique ID to identify the action and map them to the actionState in the state. Suggestion: Use UUIDs.
 * @param actionDescription Additional information on the action.
 * @param blockingType Regulates if the action is allowed to be executed during movement and/or parallel to other actions.
 *                     none: action can happen in parallel with others, including movement.
 *                     soft: action can happen simultaneously with others, but not while moving.
 *                     hard: no other actions can be performed while this action is running.
 * @param actionParameters Array of actionParameter-objects for the indicated action e. g. deviceId, loadId, external Triggers.
 */
case class Actions (
     actionType: String,
     actionId: String,
     actionDescription: Option[String],
     blockingType: BlockingTypeEnum.Value,
     actionParameters: Option[List[ActionParameter]]
)


/**
 * @param key The key of the action parameter.
 * @param value The value of the action parameter
 */
case class ActionParameter (
     key: String,
     value: Any
)


/**
 * @param edgeId Unique edge identification
 * @param sequenceId Number to track the sequence of nodes and edges in an order and to simplify order updates. The variable sequenceId runs across all nodes and edges of the same order and is reset when a new orderId is issued.
 * @param edgeDescription Additional information on the edge.
 * @param released True indicates that the edge is part of the base. False indicates that the edge is part of the horizon.
 * @param startNodeId The nodeId of the start node.
 * @param endNodeId The nodeId of the end node.
 * @param maxSpeed Permitted maximum speed on the edge in m/s. Speed is defined by the fastest measurement of the vehicle.
 * @param maxHeight Permitted maximum height of the vehicle, including the load, on edge in meters.
 * @param minHeight Permitted minimal height of the load handling device on the edge in meters
 * @param orientation Orientation of the AGV on the edge. The value orientationType defines if it has to be interpreted relative to the global project specific map coordinate system or tangential to the edge. In case of interpreted tangential to the edge 0.0 = forwards and PI = backwards. Example: orientation Pi/2 rad will lead to a rotation of 90 degrees. 
 *                    If AGV starts in different orientation, rotate the vehicle on the edge to the desired orientation if rotationAllowed is set to True. If rotationAllowed is False, rotate before entering the edge. If that is not possible, reject the order. 
 *                    If no trajectory is defined, apply the rotation to the direct path between the two connecting nodes of the edge. If a trajectory is defined for the edge, apply the orientation to the trajectory.
 * @param orientationType Enum {GLOBALGLOBAL, TANGENTIALTANGENTIAL}: 
 *                        "GLOBAL"- relative to the global project specific map coordinate system; 
 *                        "TANGENTIAL"- tangential to the edge. 
 *                        If not defined, the default value is "TANGENTIAL".
 * @param direction Sets direction at junctions for line-guided or wire-guided vehicles, to be defined initially (vehicle-individual).
 * @param rotationAllowed True: rotation is allowed on the edge. False: rotation is not allowed on the edge. 
 *                        Optional: No limit, if not set.
 * @param maxRotationSpeed Maximum rotation speed in rad/s. 
 *                         Optional: No limit, if not set.
 * @param length Distance of the path from startNode to endNode in meters. 
 *               Optional: This value is used by line-guided AGVs to decrease their speed before reaching a stop position.
 * @param trajectory Trajectory JSON-object for this edge as a NURBS. Defines the curve, on which the AGV should move between startNode and endNode. 
 *                   Optional: Can be omitted, if AGV cannot process trajectories or if AGV plans its own trajectory.
 * @param actions Array of action objects with detailed information.
 */
case class Edge (
     edgeId: String,
     sequenceId: Int,
     edgeDescription: Option[String],
     released: Boolean,
     startNodeId: String,
     endNodeId: String,
     maxSpeed: Option[Double],
     maxHeight: Option[Double],
     minHeight: Option[Double],
     orientation: Option[Double],
     orientationType: Option[String],
     direction: Option[String],
     rotationAllowed: Option[Boolean],
     maxRotationSpeed: Option[Double],
     length: Option[Double],
     trajectory: Option[Trajectory],
     actions: List[Actions]
){
    assert( sequenceId >= 0, "`sequenceId` must be greater than or equal to 0" )
    assert( orientation.forall(_ >= -3.14159265359), "`orientation` must be greater than or equal to -3.14159265359" )
    assert( orientation.forall(_ <= 3.14159265359), "`orientation` must be less than or equal to 3.14159265359" )
}


/**
 * Trajectory JSON-object for this edge as a NURBS. Defines the curve, on which the AGV should move between startNode and endNode. 
 * Optional: Can be omitted, if AGV cannot process trajectories or if AGV plans its own trajectory.
 *
 * @param degree Defines the number of control points that influence any given point on the curve. Increasing the degree increases continuity. If not defined, the default value is 1.
 * @param knotVector Sequence of parameter values that determines where and how the control points affect the NURBS curve. knotVector has size of number of control points + degree + 1.
 * @param controlPoints List of JSON controlPoint objects defining the control points of the NURBS, which includes the beginning and end point.
 */
case class Trajectory (
     degree: Int,
     knotVector: List[Double],
     controlPoints: List[ControlPoint]
){
    assert( degree >= 1, "`degree` must be greater than or equal to 1" )
}


/**
 * @param x X coordinate described in the world coordinate system.
 * @param y Y coordinate described in the world coordinate system.
 * @param weight The weight, with which this control point pulls on the curve. When not defined, the default will be 1.0.
 */
case class ControlPoint (
     x: Double,
     y: Double,
     weight: Option[Double]
){
    assert( weight.forall(_ >= 0), "`weight` must be greater than or equal to 0" )
}

object BlockingTypeEnum extends Enumeration {
    val NONE, SOFT, HARD = Value
}

/**
 * Describes an action that the AGV can perform.
 *
 * @param actionType Name of action as described in the first column of "Actions and Parameters". Identifies the function of the action.
 * @param actionId Unique ID to identify the action and map them to the actionState in the state. Suggestion: Use UUIDs.
 * @param actionDescription Additional information on the action.
 * @param blockingType Regulates if the action is allowed to be executed during movement and/or parallel to other actions.
 *                     none: action can happen in parallel with others, including movement.
 *                     soft: action can happen simultaneously with others, but not while moving.
 *                     hard: no other actions can be performed while this action is running.
 * @param actionParameters Array of actionParameter-objects for the indicated action e. g. deviceId, loadId, external Triggers.
 */
case class Actions (
     actionType: String,
     actionId: String,
     actionDescription: Option[String],
     blockingType: BlockingTypeEnum.Value,
     actionParameters: Option[List[ActionParameter]]
)


/**
 * @param key The key of the action parameter.
 * @param value The value of the action parameter
 */
case class ActionParameter (
     key: String,
     value: Any
)

