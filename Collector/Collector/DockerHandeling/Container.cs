using Collector.MeasurementExecution;
using Google.Protobuf.Collections;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Collector.DockerHandeling
{
    internal class Container
    {
        public string UUID { get; private set; }
        public int CollectorPort { get; private set; }
        public string Service { get; private set; }
        public Dictionary<string, int> Ports { get; private set; }
        public Dictionary<string, Boundries> ParameterBoundries { get; set; }

        public Container(string id, Dictionary<string, int> ports, string service)
        {
            UUID = id;
            Service = service;
            Ports = ports;
            CollectorPort = ports["metrics"];
            ParameterBoundries = new Dictionary<string, Boundries>();
        }
    }
}
