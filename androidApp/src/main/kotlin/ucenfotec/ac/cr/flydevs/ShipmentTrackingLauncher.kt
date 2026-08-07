package ucenfotec.ac.cr.flydevs

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class ShipmentTrackingLauncher(
    private val activity: ComponentActivity
) {

    private var pendingBatchDocumentId: String? = null
    private var pendingCourierId: String? = null

    private val locationPermissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[
                    Manifest.permission.ACCESS_FINE_LOCATION
                ] == true

            val coarseGranted =
                permissions[
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ] == true

            if (fineGranted || coarseGranted) {

                val batchDocumentId =
                    pendingBatchDocumentId

                val courierId =
                    pendingCourierId

                if (
                    !batchDocumentId.isNullOrBlank() &&
                    !courierId.isNullOrBlank()
                ) {
                    startService(
                        batchDocumentId = batchDocumentId,
                        courierId = courierId
                    )
                }
            }

            pendingBatchDocumentId = null
            pendingCourierId = null
        }

    fun startTracking(
        batchDocumentId: String,
        courierId: String
    ) {
        if (
            batchDocumentId.isBlank() ||
            courierId.isBlank()
        ) {
            return
        }

        if (hasLocationPermission()) {
            startService(
                batchDocumentId = batchDocumentId,
                courierId = courierId
            )

            return
        }

        pendingBatchDocumentId =
            batchDocumentId

        pendingCourierId =
            courierId

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun hasLocationPermission(): Boolean {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    private fun startService(
        batchDocumentId: String,
        courierId: String
    ) {

        val intent =
            Intent(
                activity,
                ShipmentLocationService::class.java
            ).apply {

                putExtra(
                    ShipmentLocationService
                        .EXTRA_BATCH_DOCUMENT_ID,
                    batchDocumentId
                )

                putExtra(
                    ShipmentLocationService
                        .EXTRA_COURIER_ID,
                    courierId
                )
            }

        ContextCompat.startForegroundService(
            activity,
            intent
        )

        println(
            "SHIPMENT_TRACKING | " +
                    "Tracking iniciado | " +
                    "batch=$batchDocumentId | " +
                    "courier=$courierId"
        )
    }
}