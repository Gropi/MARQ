namespace CommonLibrary.Communication.DataModel
{
    public class ConnectionInformation
    {
        public string Address { get; set; }
        public int Port { get; set; }

        public ConnectionInformation(string iPAddress, int? port)
        {
            Address = iPAddress;
            Port = port ?? default;
        }

        public ConnectionInformation() : this("localhost", 2000) { }

        // override object.Equals
        public override bool Equals(object obj)
        {
            if (obj == null || GetType() != obj.GetType())
                return false;

            var other = obj as ConnectionInformation;
            if (other != null && other.Address.Equals(Address) && other.Port.Equals(Port))
                return true;

            return base.Equals(obj);
        }

        // override object.GetHashCode
        public override int GetHashCode()
        {
            return base.GetHashCode();
        }
    }
}
