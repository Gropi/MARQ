using Collector.Communication.DataModel;
using CommonLibrary.Communication.DataModel;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Net.NetworkInformation;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace Collector.MeasurementExecution.PingExecution.impl
{
    internal class ICMPPings : IRunPing
    {
        #region Object variable

        private List<ConnectionInformation> m_Targets;

        #endregion

        #region Events 

        public event EventHandler<NetworkMeasurement> MeasurementCompleted;

        #endregion

        #region Construtor

        public ICMPPings()
        {
            m_Targets = new List<ConnectionInformation>();
        }

        #endregion

        #region Eventhandler

        private void PingCompletedCallback(object sender, PingCompletedEventArgs e, TaskCompletionSource<NetworkMeasurement> taskCompletionSource, double id)
        {
            // If the operation was canceled, display a message to the user.
            if (e.Cancelled)
            {
                Console.WriteLine("Ping canceled.");

                // Let the main thread resume.
                // UserToken is the AutoResetEvent object that the main thread
                // is waiting for.
                ((AutoResetEvent)e.UserState).Set();
            }

            // If an error occurred, display the exception to the user.
            if (e.Error != null)
            {
                Console.WriteLine("Ping failed:");
                Console.WriteLine(e.Error.ToString());

                // Let the main thread resume.
                ((AutoResetEvent)e.UserState).Set();
            }

            var reply = e.Reply;

            var measurement = CreateMeasurement(reply);
            measurement.Id = id;

            // Let the main thread resume.
            ((AutoResetEvent)e.UserState).Set();

            NotifyListeners(measurement);

            taskCompletionSource.SetResult(measurement);
        }

        #endregion

        public async Task<NetworkMeasurement> PingAsync(ConnectionInformation address, double id)
        {
            var tcs = new TaskCompletionSource<NetworkMeasurement>();
            var waiter = new AutoResetEvent(false);
            var data = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
            var buffer = Encoding.ASCII.GetBytes(data);
            var options = new PingOptions(64, true);
            var ping = new Ping();

            ping.PingCompleted += (obj, sender) =>
            {
                PingCompletedCallback(obj, sender, tcs, id);
            };

            ping.SendAsync(address.Address, 1200, buffer, options, waiter);
            return await tcs.Task;
        }

        private NetworkMeasurement CreateMeasurement(PingReply reply)
        {
            if (reply == null)
                return null;

            var measurement = new NetworkMeasurement();
            if (reply.Status == IPStatus.Success)
            {
                measurement.TargetAddress = reply.Address.ToString();
                measurement.Rtt = reply.RoundtripTime;
                measurement.Ttl = reply.Options != null ? reply.Options.Ttl : -1;
                measurement.BufferLength = reply.Buffer.Length;
                var dateTime = DateTime.Now;
                measurement.Date = dateTime.ToString("d");
                measurement.Time = dateTime.ToString("HH:mm:ss.fff", CultureInfo.CurrentCulture);
                measurement.MeasurementType = NetworkRequestType.Icmp;
            }
            return measurement;
        }

        public async void PingAllAsync(double id)
        {
            foreach (var target in m_Targets)
            {
                await PingAsync(target, id);
            }
        }

        #region Listener handler

        public void AddPingTarget(ConnectionInformation target)
        {
            var item = m_Targets.Find(x => x.Equals(target));
            if (item == null)
            {
                // Check whether this works as aspected...
                m_Targets.Add(target);
            }
        }

        public void RemovePingTarget(ConnectionInformation target)
        {
            m_Targets.Remove(target);
        }

        private void NotifyListeners(NetworkMeasurement measurement)
        {
            MeasurementCompleted?.Invoke(this, measurement);
        }

        #endregion
    }
}
