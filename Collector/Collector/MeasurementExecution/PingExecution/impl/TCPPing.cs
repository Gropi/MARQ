using Collector.Communication;
using Collector.Communication.DataModel;
using CommonLibrary.Communication.DataModel;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.Net;
using System.Net.Sockets;
using System.Threading.Tasks;

namespace Collector.MeasurementExecution.PingExecution.impl
{
    internal class TCPPing : IRunPing
    {
        #region Object variable

        private List<ConnectionInformation> m_Targets;

        #endregion

        #region Construtor

        public TCPPing()
        {
            m_Targets = new List<ConnectionInformation>();
        }

        #endregion

        #region Events

        public event EventHandler<NetworkMeasurement> MeasurementCompleted;

        #endregion

        public async void PingAllAsync(double id)
        {
            foreach (var target in m_Targets)
            {
                await PingAsync(target, id);
            }
        }

        public async Task<NetworkMeasurement> PingAsync(ConnectionInformation address, double id)
        {
            var hostInfo = Dns.GetHostEntry(address.Address);
            var endpoint = new IPEndPoint(hostInfo.AddressList[0], address.Port);

            var measurement = await MeasureRTTAsync(endpoint);
            measurement.Id = id;
            measurement.TargetAddress = endpoint.Address.ToString();

            return measurement;
        }

        private async Task<NetworkMeasurement> MeasureRTTAsync(EndPoint endPoint)
        {
            var sock = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            sock.Blocking = false;

            var stopwatch = new Stopwatch();

            // Measure the Connect call only
            stopwatch.Start();
            await sock.ConnectAsync(endPoint);
            stopwatch.Stop();

            sock.Close();

            var t = stopwatch.Elapsed.TotalMilliseconds;

            var measurement = new NetworkMeasurement();
            measurement.Rtt = Convert.ToInt64(t);
            var dateTime = DateTime.Now;
            measurement.Date = dateTime.ToString("d");
            measurement.Time = dateTime.ToString("HH:mm:ss.fff", CultureInfo.CurrentCulture);
            measurement.MeasurementType = NetworkRequestType.Tcp;

            NotifyListeners(measurement);

            return measurement;
        }

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
    }
}
