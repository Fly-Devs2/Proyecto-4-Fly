package ucenfotec.ac.cr.flydevs

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.BatchStatus
import ucenfotec.ac.cr.flydevs.domain.model.DeliveryBatch
import ucenfotec.ac.cr.flydevs.getEpochMillis


class ShipmentLocationService: Service() {

    private val serviceScope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.IO
        )
    private val firestore by lazy {
        Firebase.firestore
    }
    private var trackingStopping = false

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private var batchDocumentId: String = ""

    private var courierId: String = ""

    private var batchObserverJob: Job? = null

    private var locationUpdatesStarted = false


    /**
     * Recibe las posiciones que Android obtiene
     * mediante FusedLocationProviderClient.
     */



    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(
            result: LocationResult
        ) {
            println(
                "GPS_TEST | onLocationResult recibido | " +
                        "locations=${result.locations.size}"
            )
            val location = result.lastLocation ?:run{
                println(
                    "GPS_TEST | lastLocation = null"
                )
             return
            }
            println(
                "GPS_TEST | " +
                        "lat=${location.latitude} | " +
                        "lng=${location.longitude} | " +
                        "accuracy=${location.accuracy}"
            )
            if (
                batchDocumentId.isBlank() || courierId.isBlank()
            ) {
                return
            }
            serviceScope.launch {
                try {
                    firestore.collection(SHIPMENT_LOCATIONS_COLLECTION)
                        .document(batchDocumentId)
                        .update(
                            mapOf(
                                "courierId" to courierId,

                                "latitude" to
                                        location.latitude,

                                "longitude" to
                                        location.longitude,

                                "accuracyMeters" to
                                        location.accuracy
                                            .toDouble(),

                                "batchStatus" to
                                        BatchStatus
                                            .IN_TRANSIT
                                            .name,

                                "trackingActive" to true,

                                "updatedAt" to
                                        getEpochMillis()
                            )
                        )
                    println(
                        "GPS_TEST | FIRESTORE UPDATED | " +
                                "lat=${location.latitude} | " +
                                "lng=${location.longitude}"
                    )

                } catch (e: Exception) {
                    println(
                        "SHIPMENT_LOCATION | " +
                                "batch=$batchDocumentId | " +
                                "courier=$courierId | " +
                                "error=${e.message}"
                    )
                }
            }

        }

    }
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val receivedBatchId =
            intent?.getStringExtra(
                EXTRA_BATCH_DOCUMENT_ID
            ).orEmpty()

        val receivedCourierId =
            intent?.getStringExtra(
                EXTRA_COURIER_ID
            ).orEmpty()

        /*
         * Si Android vuelve a entregar el último Intent,
         * recuperamos estos valores.
         */
        if (receivedBatchId.isNotBlank()) {
            batchDocumentId =
                receivedBatchId
        }

        if (receivedCourierId.isNotBlank()) {
            courierId =
                receivedCourierId
        }

        if (
            batchDocumentId.isBlank() ||
            courierId.isBlank()
        ) {
            println(
                "SHIPMENT_LOCATION_ERROR | " +
                        "No se recibió batchDocumentId o courierId."
            )

            stopSelf()

            return START_NOT_STICKY
        }

        /*
         * El servicio debe pasar a foreground
         * inmediatamente.
         */
        startForeground(
            NOTIFICATION_ID,
            buildNotification()
        )

        startLocationUpdates()

        observeBatchStatus()

        /*
         * Si Android mata el proceso, intentará
         * volver a entregar el último Intent.
         */
        return START_REDELIVER_INTENT
    }



    /**
     * Empieza a recibir posiciones GPS.
     */
    private fun startLocationUpdates() {

        if (locationUpdatesStarted) {
            return
        }

        val fineLocationGranted =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted =
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (
            !fineLocationGranted &&
            !coarseLocationGranted
        ) {

            println(
                "SHIPMENT_LOCATION_ERROR | " +
                        "No existen permisos de ubicación."
            )

            stopSelf()

            return
        }

        val locationRequest =
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_INTERVAL_MS
            )
                .setMinUpdateIntervalMillis(
                    MIN_UPDATE_INTERVAL_MS
                )
                .setMinUpdateDistanceMeters(
                    MIN_DISTANCE_METERS
                )
                .build()

        fusedLocationClient
            .requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        locationUpdatesStarted = true

        println(
            "SHIPMENT_LOCATION | " +
                    "Tracking iniciado para lote " +
                    batchDocumentId
        )
    }
    /**
     * Observa el documento del lote.
     *
     * Cuando el mensajero sube evidencia
     * de entrega, BatchRepositoryImpl cambia:
     *
     * IN_TRANSIT -> DELIVERED
     *
     * El servicio detecta ese cambio y
     * detiene automáticamente el GPS.
     */
    private fun observeBatchStatus() {

        batchObserverJob?.cancel()

        batchObserverJob =
            serviceScope.launch {

                try {
                    firestore
                        .collection(
                            BATCHES_COLLECTION
                        )
                        .document(batchDocumentId)
                        .snapshots
                        .collectLatest { snapshot ->

                            if (!snapshot.exists) {
                                return@collectLatest
                            }

                            val batch =
                                runCatching {
                                    snapshot
                                        .data<DeliveryBatch>()
                                }
                                    .getOrNull()
                                    ?: return@collectLatest

                            println(
                                "SHIPMENT_LOCATION | " +
                                        "Estado lote: ${batch.status}"
                            )

                            when (batch.status) {

                                BatchStatus.DELIVERED -> {
                                    stopTracking(
                                        finalStatus =
                                            BatchStatus.DELIVERED
                                    )
                                }

                                BatchStatus.CANCELLED -> {
                                    stopTracking(
                                        finalStatus =
                                            BatchStatus.CANCELLED
                                    )
                                }

                                else -> Unit
                            }
                        }

                } catch (exception: Exception) {

                    println(
                        "SHIPMENT_LOCATION_ERROR | " +
                                "Error observando lote | " +
                                exception.message
                    )
                }
            }
    }
    /**
     * Marca el documento como detenido,
     * pero NO elimina las últimas coordenadas.
     */
    private fun stopTracking(
        finalStatus: BatchStatus
    ) {

        if (trackingStopping) {
            return
        }

        trackingStopping = true

        if (batchDocumentId.isBlank()) {
            stopLocationUpdates()
            stopSelf()
            return
        }

        /*
         * Primero detenemos el GPS para evitar que
         * llegue otra posición mientras estamos
         * cerrando el tracking.
         */
        stopLocationUpdates()

        serviceScope.launch {

            try {

                firestore
                    .collection(
                        SHIPMENT_LOCATIONS_COLLECTION
                    )
                    .document(batchDocumentId)
                    .update(
                        mapOf(
                            "trackingActive" to false,

                            "batchStatus" to
                                    finalStatus.name,

                            "updatedAt" to
                                    getEpochMillis()
                        )
                    )

                println(
                    "SHIPMENT_LOCATION | " +
                            "Tracking finalizado | " +
                            "batch=$batchDocumentId | " +
                            "status=${finalStatus.name}"
                )

            } catch (exception: Exception) {

                println(
                    "SHIPMENT_LOCATION_ERROR | " +
                            "No se pudo actualizar " +
                            "trackingActive=false | " +
                            exception.message
                )

            } finally {

                batchObserverJob?.cancel()
                batchObserverJob = null

                stopForeground(
                    STOP_FOREGROUND_REMOVE
                )

                stopSelf()
            }
        }
    }
    private fun stopLocationUpdates() {

        if (!locationUpdatesStarted) {
            return
        }

        fusedLocationClient
            .removeLocationUpdates(
                locationCallback
            )

        locationUpdatesStarted = false
    }

    /**
     * Canal requerido para la notificación
     * permanente del Foreground Service.
     */
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Ubicación de envíos",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description =
                        "Seguimiento de ubicación de envíos en curso"
                }

            val manager =
                getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }
    private fun buildNotification() =
        NotificationCompat
            .Builder(
                this,
                NOTIFICATION_CHANNEL_ID
            )
            .setContentTitle(
                "FlyDevs"
            )
            .setContentText(
                "Compartiendo ubicación del envío"
            )
            .setSmallIcon(
                android.R.drawable
                    .ic_menu_mylocation
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(
                NotificationCompat
                    .PRIORITY_LOW
            )
            .build()

    override fun onDestroy() {

        stopLocationUpdates()

        batchObserverJob?.cancel()

        serviceScope.cancel()

        println(
            "SHIPMENT_LOCATION | " +
                    "ShipmentLocationService destruido"
        )

        super.onDestroy()
    }
    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    companion object {

        /**
         * Extras que enviaremos cuando el
         * mensajero presione "Iniciar ruta".
         */
        const val EXTRA_BATCH_DOCUMENT_ID =
            "batch_document_id"

        const val EXTRA_COURIER_ID =
            "courier_id"

        private const val BATCHES_COLLECTION =
            "batches"

        private const val SHIPMENT_LOCATIONS_COLLECTION =
            "shipment_locations"

        private const val NOTIFICATION_CHANNEL_ID =
            "shipment_tracking"

        private const val NOTIFICATION_ID =
            4001

        /**
         * Android intentará obtener una nueva
         * posición aproximadamente cada 15 segundos.
         */
        private const val LOCATION_INTERVAL_MS =
            15_000L

        /**
         * Permitimos una ubicación anticipada
         * si Android obtiene una buena posición.
         */
        private const val MIN_UPDATE_INTERVAL_MS =
            10_000L

        /**
         * Evita escrituras innecesarias si
         * el mensajero prácticamente no se mueve.
         */
        private const val MIN_DISTANCE_METERS =
            15f
    }








}




