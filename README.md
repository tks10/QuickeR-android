# QuickeR-android

A simple QRCode management app for Android

## API specification
* getQRCodes(): List<QRCode>
    - Get all of registered QR codes.
    
* getQRCode(id: String): QRCode
    - Get QR code which id is matched argument's id.
    
* saveQRCode(code: QRCode, image: Bitmap): Boolean
    - Save QR code and its image. This operation overwrite QR code which has same id as argument's one.
    - If all of the process succeeded, it returns true.
    
* saveQRCode(serviceId: Int, qrImage: Bitmap): Boolean
    - Save QR code and its image by serviceId. It also overrides old one.
    - This method generally be used to save QR codes for Default Service.
    - If all of the processes succeeded, it returns true.
    
* saveQRCode(serviceName: String, qrImage: Bitmap, iconImage: Bitmap): Boolean
    - Save QR code and its image and the service image by serviceName. It also overrides old one.
    - This method generally be used to save QR codes for User Service.
    - If all of the processes succeeded, it returns true.
    
* deleteQRCode(id: String): Boolean
    - Delete QR code by id from SharedPreferences.
    - This method dose not delete image files from the Storage. CAUTION!
    
* doneTutorial(component: TutorialComponent): Unit
    - Notify DB that the tutorial has been done.
    
* hasDoneTutorial(component: TutorialComponent): Boolean
    - Check the tutorial has done.
    - If the tutorial has done, it returns true.
    
* updateQRCodesOrder(indexes: List<Int>): Unit
    - Update order of the QR Codes.
    - User gives this method the list that explains relative order of the codes.
