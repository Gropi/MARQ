using Docker.DotNet;
using Docker.DotNet.Models;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace Collector.DockerHandeling
{
    internal class DockerHandler
    {
        private DockerClient m_Client;
        private int m_PortCount;
        private int m_BasePort;
        private int m_ContainerCount;

        public DockerHandler(int basePort)
        {
            m_Client = new DockerClientConfiguration(
                    //new Uri("npipe://./pipe/docker_engine")       //WINDOWS
                    new Uri("unix:///var/run/docker.sock")          //LINUX
                ).CreateClient();
            m_PortCount = 0;
            m_ContainerCount = 0;
            m_BasePort = basePort;
        }
        
        public async Task<Dictionary<string, int>> startObjectRecognitionDocker(string id, string endpoint, string volumeName)
        {
            var containerPorts = new Dictionary<string, int>();
            containerPorts.Add("inference", m_BasePort + m_PortCount);
            containerPorts.Add("management", m_BasePort + m_PortCount + 1);
            containerPorts.Add("metrics", m_BasePort + m_PortCount + 2);

            //INITIALIZE PORT BINDINGS
            var inferencePorts = new List<PortBinding>();
            var managementPorts = new List<PortBinding>();
            var collectorPorts = new List<PortBinding>();

            inferencePorts.Add(new PortBinding { HostPort = containerPorts["inference"].ToString() });
            managementPorts.Add(new PortBinding { HostPort = containerPorts["management"].ToString() });
            collectorPorts.Add(new PortBinding { HostPort = containerPorts["metrics"].ToString() });

            var portBindings = new Dictionary<string, IList<PortBinding>>();
            portBindings.Add("2002", inferencePorts);
            portBindings.Add("8081", managementPorts);
            portBindings.Add("2003", collectorPorts);

            var exposedPorts = new Dictionary<string, EmptyStruct>();
            exposedPorts.Add("2002", new EmptyStruct());
            exposedPorts.Add("8081", new EmptyStruct());
            exposedPorts.Add("2003", new EmptyStruct());

            //INITIALIZE BINDS
            var bindList = new List<string>();
            bindList.Add(volumeName + ":/home/model-server/model-store");

            //INITIALIZE CMD
            var commandList = new List<string>();
            commandList.Add("serveAndCollect");
            commandList.Add(endpoint);
            commandList.Add(id);

            await m_Client.Containers.CreateContainerAsync(new CreateContainerParameters()
            {
                Name = "Container" + m_ContainerCount,
                Image = "plan_b:latest",
                HostConfig = new HostConfig()
                {
                    Binds = bindList,
                    PortBindings = portBindings,
                    AutoRemove = true,

                    Memory = 12500000000,
                    MemorySwap = -1,

                    CPUPeriod = 100 * 1000,
                    CPUQuota = 500 * 1000,
                },
                ExposedPorts = exposedPorts,
                Cmd = commandList,
            });

            await m_Client.Containers.StartContainerAsync("Container" + m_ContainerCount, new ContainerStartParameters());

            m_PortCount = m_PortCount + 3;
            m_ContainerCount++;

            return containerPorts;
        }

        public async Task<Dictionary<string, int>> startFaceDetectionDocker(string id, string endpoint)
        {
            var containerPorts = new Dictionary<string, int>();
            containerPorts.Add("inference", m_BasePort + m_PortCount);
            containerPorts.Add("metrics", m_BasePort + m_PortCount + 1);

            //INITIALIZE PORT BINDINGS
            var inferencePorts = new List<PortBinding>();
            var collectorPorts = new List<PortBinding>();

            inferencePorts.Add(new PortBinding { HostPort = containerPorts["inference"].ToString() });
            collectorPorts.Add(new PortBinding { HostPort = containerPorts["metrics"].ToString() });

            var portBindings = new Dictionary<string, IList<PortBinding>>();
            portBindings.Add("2002", inferencePorts);
            portBindings.Add("2003", collectorPorts);

            var exposedPorts = new Dictionary<string, EmptyStruct>();
            exposedPorts.Add("2002", new EmptyStruct());
            exposedPorts.Add("2003", new EmptyStruct());

            //INITIALIZE CMD
            var commandList = new List<string>();
            commandList.Add("serveAndCollect");
            commandList.Add(endpoint);
            commandList.Add(id);

            await m_Client.Containers.CreateContainerAsync(new CreateContainerParameters()
            {
                Name = "Container" + m_ContainerCount,
                Image = "stasi_uwe:latest",
                HostConfig = new HostConfig()
                {
                    PortBindings = portBindings,
                    AutoRemove = true,

                    Memory = 12500000000,
                    MemorySwap = -1,

                    CPUPeriod = 100 * 1000,
                    CPUQuota = 500 * 1000,
                },
                ExposedPorts = exposedPorts,
                Cmd = commandList,
            });

            await m_Client.Containers.StartContainerAsync("Container" + m_ContainerCount, new ContainerStartParameters());

            m_PortCount = m_PortCount + 2;
            m_ContainerCount++;

            return containerPorts;
        }

        public async Task<Dictionary<string, int>> startDeploymentDocker(string id)
        {
            var containerPorts = new Dictionary<string, int>();
            containerPorts.Add("inference", m_BasePort + m_PortCount);
            containerPorts.Add("metrics", m_BasePort + m_PortCount + 1);

            //INITIALIZE PORT BINDINGS
            var inferencePorts = new List<PortBinding>();
            var collectorPorts = new List<PortBinding>();

            inferencePorts.Add(new PortBinding { HostPort = containerPorts["inference"].ToString() });
            collectorPorts.Add(new PortBinding { HostPort = containerPorts["metrics"].ToString() });

            var portBindings = new Dictionary<string, IList<PortBinding>>();
            portBindings.Add("2002", inferencePorts);
            portBindings.Add("2003", collectorPorts);

            var exposedPorts = new Dictionary<string, EmptyStruct>();
            exposedPorts.Add("2002", new EmptyStruct());
            exposedPorts.Add("2003", new EmptyStruct());

            //INITIALIZE CMD
            var commandList = new List<string>();
            commandList.Add("serveAndCollect");
            commandList.Add(id);

            await m_Client.Containers.CreateContainerAsync(new CreateContainerParameters()
            {
                Name = "Container" + m_ContainerCount,
                Image = "kleine_kratze:latest",
                HostConfig = new HostConfig()
                {
                    PortBindings = portBindings,
                    AutoRemove = true,

                    Memory = 12500000000,
                    MemorySwap = -1,

                    CPUPeriod = 100 * 1000,
                    CPUQuota = 500 * 1000,
                },
                ExposedPorts = exposedPorts,
                Cmd = commandList,
            });

            await m_Client.Containers.StartContainerAsync("Container" + m_ContainerCount, new ContainerStartParameters());

            m_PortCount = m_PortCount + 2;
            m_ContainerCount++;

            return containerPorts;
        }

        public async Task<Dictionary<string, int>> startBlurringDocker(string id)
        {
            var containerPorts = new Dictionary<string, int>();
            containerPorts.Add("inference", m_BasePort + m_PortCount);
            containerPorts.Add("metrics", m_BasePort + m_PortCount + 1);

            //INITIALIZE PORT BINDINGS
            var inferencePorts = new List<PortBinding>();
            var collectorPorts = new List<PortBinding>();

            inferencePorts.Add(new PortBinding { HostPort = containerPorts["inference"].ToString() });
            collectorPorts.Add(new PortBinding { HostPort = containerPorts["metrics"].ToString() });

            var portBindings = new Dictionary<string, IList<PortBinding>>();
            portBindings.Add("2002", inferencePorts);
            portBindings.Add("2003", collectorPorts);

            var exposedPorts = new Dictionary<string, EmptyStruct>();
            exposedPorts.Add("2002", new EmptyStruct());
            exposedPorts.Add("2003", new EmptyStruct());

            //INITIALIZE CMD
            var commandList = new List<string>();
            commandList.Add("serveAndCollect");
            commandList.Add(id);

            await m_Client.Containers.CreateContainerAsync(new CreateContainerParameters()
            {
                Name = "Container" + m_ContainerCount,
                Image = "hauptsache_schnaps:latest",
                HostConfig = new HostConfig()
                {
                    PortBindings = portBindings,
                    AutoRemove = true,

                    Memory = 12500000000,
                    MemorySwap = -1,

                    CPUPeriod = 100 * 1000,
                    CPUQuota = 500 * 1000,
                },
                ExposedPorts = exposedPorts,
                Cmd = commandList,
            });

            await m_Client.Containers.StartContainerAsync("Container" + m_ContainerCount, new ContainerStartParameters());

            m_PortCount = m_PortCount + 2;
            m_ContainerCount++;

            return containerPorts;
        }

        public async Task<Dictionary<string, int>> startDummyDocker(string id)
        {
            var containerPorts = new Dictionary<string, int>();
            containerPorts.Add("inference", m_BasePort + m_PortCount);
            containerPorts.Add("metrics", m_BasePort + m_PortCount + 1);

            //INITIALIZE PORT BINDINGS
            var inferencePorts = new List<PortBinding>();
            var collectorPorts = new List<PortBinding>();

            inferencePorts.Add(new PortBinding { HostPort = containerPorts["inference"].ToString() });
            collectorPorts.Add(new PortBinding { HostPort = containerPorts["metrics"].ToString() });

            var portBindings = new Dictionary<string, IList<PortBinding>>();
            portBindings.Add("2002", inferencePorts);
            portBindings.Add("2003", collectorPorts);

            var exposedPorts = new Dictionary<string, EmptyStruct>();
            exposedPorts.Add("2002", new EmptyStruct());
            exposedPorts.Add("2003", new EmptyStruct());

            //INITIALIZE CMD
            var commandList = new List<string>();
            commandList.Add("serveAndCollect");
            commandList.Add(id);

            await m_Client.Containers.CreateContainerAsync(new CreateContainerParameters()
            {
                Name = "Container" + m_ContainerCount,
                Image = "schnapsdrossel_quintin:latest",
                HostConfig = new HostConfig()
                {
                    PortBindings = portBindings,
                    AutoRemove = true,

                    Memory = 500000000,
                    MemorySwap = -1,

                    CPUPeriod = 100 * 1000,
                    CPUQuota = 500 * 1000,
                },
                ExposedPorts = exposedPorts,
                Cmd = commandList,
            });

            await m_Client.Containers.StartContainerAsync("Container" + m_ContainerCount, new ContainerStartParameters());

            m_PortCount = m_PortCount + 2;
            m_ContainerCount++;

            return containerPorts;
        }
    }
}
