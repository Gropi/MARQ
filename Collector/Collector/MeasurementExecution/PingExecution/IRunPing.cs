using Collector.Communication.DataModel;
using CommonLibrary.Communication.DataModel;
using System;
using System.Threading.Tasks;

namespace Collector.MeasurementExecution.PingExecution
{
    internal interface IRunPing
    {
        #region Event

        event EventHandler<NetworkMeasurement> MeasurementCompleted;

        #endregion

        Task<NetworkMeasurement> PingAsync(ConnectionInformation address, double id);

        void PingAllAsync(double id);

        #region Listeners

        void AddPingTarget(ConnectionInformation target);

        void RemovePingTarget(ConnectionInformation target);

        #endregion
    }
}
