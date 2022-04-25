[![](https://jitpack.io/v/mohamadnavabi/payment-card-scanner.svg)](https://jitpack.io/#mohamadnavabi/payment-card-scanner)

## Payment Card Scanner

A lightweight android library to scan Iranian debit cards fast and realtime using Deep Learning and TensorFlow-Lite.
This library scans valid card numbers only.
Keep in mind that split ABIs while releasing your app to reduce its size.

To check stability and scan speed, check [STABILITY.md](./STABILITY.md) file.

## Installation

Gradle:

```
dependencies {
    implementation "com.github.mohamadnavabi:payment-card-scanner:1.0.1"
}
```

## How To Use

(1) Start scanner activity and wait for result:
``` 
ScanActivity.start(this);
```

(2) Retrieve scanned data:
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (ScanActivity.isScanResult(requestCode) && resultCode == Activity.RESULT_OK && data != null) {
        DebitCard scanResult = ScanActivity.debitCardFromResult(data);
        if (scanResult != null)
            Log.d("IRDCS", scanResult.number);
    }
}
```

- ##### {Debug Mode}

Start scanner activity in debug mode:
``` 
ScanActivity.startDebug(this);
```
In this mode you will see a scanned preview while scanning.

## Contact me

If you have a better idea or way on this project, please let me know. Thanks :) ?:

- [Email](mailto:navabifar@gmail.com)


---

##### References

Based on [this](https://github.com/getbouncer/cardscan-android) project!
