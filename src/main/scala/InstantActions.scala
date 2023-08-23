
/**
 * JSON Schema for publishing instantActions that the AGV is to execute as soon as they arrive.
 *
 * @param headerId headerId of the message. The headerId is defined per topic and incremented by 1 with each sent (but not necessarily received) message.
 * @param timestamp Timestamp in ISO8601 format (YYYY-MM-DDTHH:mm:ss.ssZ).
 * @param version Version of the protocol [Major].[Minor].[Patch]
 * @param manufacturer Manufacturer of the AGV
 * @param serialNumber Serial number of the AGV.
 * @param actions
 */
case class InstantActions (
     headerId: Option[Int],
     timestamp: Option[String],
     version: Option[String],
     manufacturer: Option[String],
     serialNumber: Option[String],
     actions: Option[List[Actions]]
)

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

