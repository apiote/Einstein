package ml.adamsprogs.einstein.mobile.components;

import ml.adamsprogs.einstein.mobile.activities.Einstein;

public class NetworkMobileDie extends MobileDie {
    private Einstein application;

    public NetworkMobileDie(MobileDie d, Einstein application) {
        super(d.context, d.orientation, d.view, (MobilePlayer) d.player);
        this.application = application;
    }

    protected void attachOnClickListener() {
        this.view.setOnClickListener(null);
    }

    public void waitForRoll() {
        int roll = application.receiveRoll();
        setValue(roll);
        draw();
        signalRoll();
    }
}
