class VideoProcessingError(Exception):
    pass


class ValidationError(VideoProcessingError):
    pass


class CloudinaryError(VideoProcessingError):
    pass


class VideoFormatError(ValidationError):
    pass


class VideoSizeError(ValidationError):
    pass


class VideoDurationError(ValidationError):
    pass


class InvalidURLError(ValidationError):
    pass


class UnsupportedPlatformError(ValidationError):
    pass
