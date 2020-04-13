# Barcode Scanner
Android library that provides easy to use and extensible Barcode Scanner views based on [Google ML Kit](https://developers.google.com/ml-kit).

## Steps to add the library

* [Create a Firebase project in the Firebase console, if you don't already have one](https://firebase.google.com/docs/android/setup)
* Add a new Android app into your Firebase project
* Download the config file (google-services.json) from the new added app and move it into the module folder (i.e. [app/](./app/))
* Add camera permission to your AndroidManifest.xml file
```xml
<uses-permission android:name="android.permission.CAMERA" />
```
* Add the following dependency to your build.gradle file
```groovy
repositories {
   jcenter()
}
```
```groovy
android {
  ...
  // BarcodeScanner requires Java 8.
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}

dependencies {
  implementation 'com.neyogiry.scanner:barcode:0.1.0'
}
```

## Simple usage

A very basic activity would look like this:
```kotlin
class ScannerActivity : AppCompatActivity(), BarcodeScannerView.BarcodeResult  {

    private var preview: CameraSourcePreview? = null
    private var graphicOverlay: GraphicOverlay? = null
    private var barcodeScanner: BarcodeScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        preview = findViewById(R.id.camera_preview)
        graphicOverlay = findViewById<GraphicOverlay>(R.id.camera_preview_graphic_overlay)
        barcodeScanner = BarcodeScannerView(this)
        barcodeScanner?.onCreate(preview!!, graphicOverlay!!)
        barcodeScanner?.setBarcodeResult(this)
    }

    override fun onResume() {
        super.onResume()
        barcodeScanner?.onResume()
    }

    override fun onPause() {
        super.onPause()
        barcodeScanner?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        barcodeScanner?.onDestroy()
    }

    override fun onBarcodeResult(value: String) {
        Log.i(TAG, "The value is $value)
    }

}
```

## License
Licensed under an [Apache-2](./LICENSE) license.
