package kr.co.iefriends.pcsx2;

public class HIDDeviceManager {

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////// Native methods
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    private native void HIDDeviceRegisterCallback();
    private native void HIDDeviceReleaseCallback();

    native void HIDDeviceConnected(int deviceID, String identifier, int vendorId, int productId, String serial_number, int release_number, String manufacturer_string, String product_string, int interface_number, int interface_class, int interface_subclass, int interface_protocol, boolean bBluetooth);
    native void HIDDeviceOpenPending(int deviceID);
    native void HIDDeviceOpenResult(int deviceID, boolean opened);
    native void HIDDeviceDisconnected(int deviceID);

    native void HIDDeviceInputReport(int deviceID, byte[] report);
    native void HIDDeviceReportResponse(int deviceID, byte[] report);
}
