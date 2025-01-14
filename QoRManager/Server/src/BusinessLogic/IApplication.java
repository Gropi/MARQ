package BusinessLogic;

import Network.Connection.IConnectionInformation;
import Network.DataModel.CommunicationMessages;

import java.io.IOException;
import java.util.List;

public interface IApplication extends Runnable {

    boolean isRunning();

    void onMeasurementUpdate(CommunicationMessages.MeasurementEvent measurementEvent, IConnectionInformation from);

    boolean onMicroserviceTerminated(String senderID, List<CommunicationMessages.ProcessState> processStates, CommunicationMessages.TerminationMessage terminationMessage) throws InterruptedException, IOException;

    void exit();

}
