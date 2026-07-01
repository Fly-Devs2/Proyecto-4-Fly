package ucenfotec.ac.cr.flydevs.presentation.publishGameCard

/**
 * Feedback general del proceso de publicación. Es semántico: la capa de UI decide
 * el texto y el color, así el ViewModel queda libre de copy y se puede localizar.
 */
enum class PublishFeedback {
    SUCCESS,
    MISSING_FIELDS,
    PUBLISH_FAILED,
}
