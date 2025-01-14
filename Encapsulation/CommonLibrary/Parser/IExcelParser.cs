using Collector.Parser.Objects;

namespace CommonLibrary.Parser
{
    public interface IExcelParser
    { 
        IList<string[]> ParseXLS(string filepath);

        IList<string[]> ParseCSV(string fileNameWithPath);

        void AppendToCSVFile(string[] content, string filePath);
        void WriteCSVFile(IList<string[]> content, string filePath, bool appendText);
        void WriteCSVFile(IList<string[]> content, string filePath, bool appendText, bool overrideFile);
        void WriteCSVFile(IDictionary<string, string> parameters, string fileToSaveTo, bool overrideFile);

        char CSVSeperator { get; set; }
        
        void WriteToCSV<T>(IList<T> entries, string[] header, string filePath, bool appendText, bool overrideFile) where T : IExcelParsable;
        void WriteToCSV<T>(IList<T> entries, string filePath, bool appendText, bool overrideFile) where T : IExcelParsable;

        IDictionary<string, IList<string[]>> ReadXLSXFile(string fileToReadFrom);
        IDictionary<string, IList<string[]>> ReadXLSXFile(string fileToReadFrom, int indexOfSheet);
        IDictionary<string, IList<string[]>> ReadXLSXFile(string fileToReadFrom, string sheetName);
    }
}
