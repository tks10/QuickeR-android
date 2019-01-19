# QuickeR-android

A simple QRCode management app for Android

## API specification
* getQRCodes(): List<QRCode>
    Get all of registered QR codes.
* getQRCode(id: String): QRCode
    Get QR code which id is id by argument.
* saveQRCode(code: QRCode, image: Bitmap): Boolean
    Save QR code and that image. This operation overwrite QR code which has same id as argument's one.
    If all of the process succeeded, it returns true.
