using Collector.Communication.DataModel;
using CommonLibrary.Communication.DataModel;
using NLog;
using System;
using System.Collections.Concurrent;
using System.Timers;
using Timer = System.Timers.Timer;

namespace Collector.MeasurementExecution.PingExecution.impl
{
    internal class PingExecutor : IMeasurmenetUnit
    {
        #region Variables

        private Logger m_Logger;
        private ConcurrentDictionary<NetworkRequestType, IRunPing> m_RunningPingMeasurements;

        private int m_ElapsedTimer = 0;
        private Timer m_Timer;
        private object m_LockObject = new object();

        #endregion

        #region Event

        public event EventHandler<MeasurementEvent> OnMeasurmentHappens;

        #endregion

        #region Constructor

        public PingExecutor(Logger logger)
        {
            m_Logger = logger;
            m_RunningPingMeasurements = new ConcurrentDictionary<NetworkRequestType, IRunPing>();

            InitMeasurementUnit(NetworkRequestType.Icmp, new ICMPPings());
            InitMeasurementUnit(NetworkRequestType.Tcp, new TCPPing());

            StartTimer(10000);
        }

        #endregion

        #region public methods

        public void StopMeasurement()
        {
            m_Timer.Stop();
        }

        #endregion

        #region Eventhandler

        // Specify what you want to happen when the Elapsed event is raised.
        private void OnTimedEvent(object source, ElapsedEventArgs e)
        {
            var id = NextCounter();
            foreach (var key in m_RunningPingMeasurements.Keys)
            {
                IRunPing runPing;
                m_RunningPingMeasurements.TryGetValue(key, out runPing);
                runPing?.PingAllAsync(id);
            }
            // TODO: Checken, ob wir hier abbrechen müssen
        }

        #endregion

        #region private methods

        private void InitMeasurementUnit(NetworkRequestType type, IRunPing pingMeasurement)
        {
            pingMeasurement.MeasurementCompleted += MeassurementCompleted;
            m_RunningPingMeasurements.TryAdd(type, pingMeasurement);
        }

        private int NextCounter()
        {
            int nextCounter;
            lock (m_LockObject)
            {
                m_ElapsedTimer++;
                nextCounter = m_ElapsedTimer;
            }
            return nextCounter;
        }

        private void StartTimer(int interval)
        {
            m_Timer = new Timer();

            m_Timer.Elapsed += new ElapsedEventHandler(OnTimedEvent);
            m_Timer.Interval = interval;
            m_Timer.Enabled = true;

            m_Timer.Start();
        }

        #endregion

        #region Handle listeners

        private void MeassurementCompleted(object sender, NetworkMeasurement measurement)
        {
            NotifyListeners(measurement);
        }

        public void RemoveListener(SubscriptionLifecycle listener)
        {
            var endpoint = CreateConnectionInformation(listener);
            var sub = listener.Unsubscribe;
            var type = sub.Request.Type;
            IRunPing runPing;
            m_RunningPingMeasurements.TryGetValue(type, out runPing);
            runPing?.RemovePingTarget(endpoint);
        }

        public void UpdateListener(SubscriptionLifecycle listener)
        {
            throw new NotImplementedException();
        }

        public void AddListener(SubscriptionLifecycle listener)
        {
            var sub = listener.Sub;
            var type = sub.Request.Type;
            var endpoint = CreateConnectionInformation(listener);
            IRunPing runPing;
            m_RunningPingMeasurements.TryGetValue(type, out runPing);
            runPing?.AddPingTarget(endpoint);
        }

        private void NotifyListeners(NetworkMeasurement measurement)
        {
            var measurementEvent = new MeasurementEvent();
            measurementEvent.Network = measurement;
            OnMeasurmentHappens?.Invoke(this, measurementEvent);
        }

        #endregion

        private ConnectionInformation CreateConnectionInformation(SubscriptionLifecycle listener)
        {
            if (listener.Sub != null)
                return CreateConnectionInformationForNetworkRequest(listener.Sub.Request);
            else if (listener.Unsubscribe != null)
                return CreateConnectionInformationForNetworkRequest(listener.Unsubscribe.Request);

            return null;
        }

        private ConnectionInformation CreateConnectionInformationForNetworkRequest(NetworkRequest request)
        {
            var endpoint = request.Target;
            var port = request.Port;
            return new ConnectionInformation(endpoint, port);
        }
    }
}
