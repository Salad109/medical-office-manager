package io.salad109.medicalofficemanager.exception.internal

import io.salad109.medicalofficemanager.exception.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.Instant


@ControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: ResourceNotFoundException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = ex.message ?: "Resource not found"
        val errorResponse = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = message,
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
            .also { log.info("ResourceNotFoundException: $message") }
    }

    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExistsException(
        ex: ResourceAlreadyExistsException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = ex.message ?: "Resource already exists"
        val errorResponse = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.CONFLICT.value(),
            error = HttpStatus.CONFLICT.reasonPhrase,
            message = message,
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
            .also { log.info("ResourceAlreadyExistsException: $message") }
    }

    @ExceptionHandler(
        InvalidRoleException::class,
        ValidationException::class,
        InvalidTimeSlotException::class,
        InvalidAppointmentStatusException::class,
        IllegalArgumentException::class
    )
    fun handleValidationExceptions(
        ex: Exception, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = ex.message ?: "Validation error"
        val errorResponse = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = message,
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
            .also { log.info("ValidationException: $message") }
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = ex.message ?: "Access denied"
        val errorResponse = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.FORBIDDEN.value(),
            error = HttpStatus.FORBIDDEN.reasonPhrase,
            message = message,
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
            .also { log.info("AccessDeniedException: $message") }
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentialsException(
        ex: BadCredentialsException, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = ex.message ?: "Invalid credentials"
        val errorResponse = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = HttpStatus.UNAUTHORIZED.reasonPhrase,
            message = message,
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
            .also { log.info("BadCredentialsException: $message") }
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception, request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val message = ex.message ?: "Internal server error"
        val errorResponse = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
            message = message,
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
            .also { log.error("Unhandled exception", ex) }
    }
}