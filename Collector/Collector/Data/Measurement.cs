using Collector.Parser.Objects;
using System.Collections.Generic;
using System.Net.NetworkInformation;
using System.Text;

namespace Collector.Data
{
    public class Measurement : IExcelParsable
    {
        #region Properties

        public IPStatus Status { get; set; }

        public double ID { get; set; }

        public long RTT { get; set; }

        public string Sender { get; set; }

        public string SenderType { get; set; }

        public string TargetAddress { get; set; }

        public string Lattitude { get; set; }

        public string Longitude { get; set; }

        public string Date { get; set; }

        public string Time { get; set; }

        public int? TTL { get; set; }

        public int BufferLength { get; set; }

        #endregion

        #region Methods

        public string GetMeasurementAsString()
        {
            var measurement = new StringBuilder();
            measurement.Append("ping status: {0}" + Status);
            measurement.Append("ID: {0}" + ID);
            if (Status == IPStatus.Success)
            {
                measurement.Append("Address to: {0}" + TargetAddress);
                measurement.Append("RTT: {0}" + RTT);
                measurement.Append("TTL: {0}" + TTL);
                measurement.Append("Buffer size: {0}" + BufferLength);
                measurement.Append("Longitude: {0}" + Longitude);
                measurement.Append("Lattitude: {0}" + Lattitude);
                measurement.Append("Sender: {0}" + Sender);
                measurement.Append("SenderType: {0}" + SenderType);
            }
            return measurement.ToString();
        }

        public string[] GetObjectAsStringArray()
        {
            var objectAsString = new List<string>();

            objectAsString.Add(ID.ToString());
            objectAsString.Add(Date);
            objectAsString.Add(Time);
            objectAsString.Add(Status.ToString());
            objectAsString.Add(TargetAddress.ToString());
            objectAsString.Add(RTT.ToString());
            objectAsString.Add(TTL.ToString());
            objectAsString.Add(BufferLength.ToString());
            objectAsString.Add(Longitude?.ToString());
            objectAsString.Add(Lattitude?.ToString());
            objectAsString.Add(Sender?.ToString());
            objectAsString.Add(SenderType?.ToString());

            return objectAsString.ToArray();
        }

        #endregion
    }
}
