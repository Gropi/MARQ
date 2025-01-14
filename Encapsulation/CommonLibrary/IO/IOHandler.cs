using System.Collections.Generic;
using System.IO;

namespace CommonLibrary.IO
{
    public interface IOHandler
    {
        #region File Methods

        /// <summary>
        /// This method checks whether the given file path exists.
        /// </summary>
        /// <param name="file">The expected file</param>
        /// <returns>True if yes, otherwise false.</returns>
        bool IsFile(string file);

        /// <summary>
        /// Returns the working path of the application with a given file.
        /// </summary>
        /// <param name="file">The file which is searched.</param>
        /// <returns>Returns the working path of the application.</returns>
        string GetPathFromApplicationWithFile(string file);

        /// <summary>
        /// This method checks whether the extension is correct or not. The extension has to start with a dot.
        /// </summary>
        /// <param name="extension">The extension to check</param>
        /// <returns>True if ok, otherwise false</returns>
        bool IsExtentionOk(string extension);

        /// <summary>
        /// This method gets all files of a given folder. If the given folder is no real folder, there will be an exception. 
        /// </summary>
        /// <param name="directory">The directory</param>
        /// <returns>A list of all entries, might be empty if there is no entry</returns>
        FileInfo[] GetAllFileObjectsFromDirectory(string directory);

        /// <summary>
        /// This method gets all files of a given folder. If the given folder is no real folder, there will be an exception. 
        /// </summary>
        /// <param name="directory">The directory</param>
        /// <param name="recursive">Only the Top Directory or all</param>
        /// <returns>A list of all entries, might be empty if there is no entry</returns>
        FileInfo[] GetAllFileObjectsFromDirectory(string directory, bool recursive);

        /// <summary>
        /// Returns all the folders of the given directory.
        /// </summary>
        IList<string> GetAllFoldersFromDirectory(string m_DestinationFolder);

        /// <summary>
        /// This method gets all files of a given folder. If the given folder is no real folder, there will be an exception. 
        /// Also when the extension is not correct.
        /// </summary>
        /// <param name="directory">The directory</param>
        /// <param name="extension">The extesion which has to be looked for</param>
        /// <returns>A list of all entries, might be empty if there is no entry</returns>
        FileInfo[] GetAllFileObjectsByExtensionFromDirectory(string directory, string extension);

        /// <summary>
        /// This method gets all files of a given folder. If the given folder is no real folder, there will be an exception. 
        /// Also when the extension is not correct.
        /// </summary>
        /// <param name="directory">The directory</param>
        /// <param name="recursive">Only the Top Directory or all</param>
        /// <param name="extension">The extesion which has to be looked for</param>
        /// <returns>A list of all entries, might be empty if there is no entry</returns>
        FileInfo[] GetAllFileObjectsByExtensionFromDirectory(string directory, bool recursive, string extension);

        /// <summary>
        /// This method reads a hole file and checks that the given file name is a real file. If not, there is an IO Exception.
        /// </summary>
        /// <param name="fileName">The file to load</param>
        /// <returns>The content of the file as String.</returns>
        string ReadAllText(string fileName);

        /// <summary>
        /// This method moves a file to a given folder. You can decide whether the file has to be overwritten or not.
        /// If the input is not correct, there will be an exception.
        /// </summary>
        /// <param name="source">The file, has to be full path to file.</param>
        /// <param name="destination">The destination path</param>
        /// <param name="overrideFile">True if yes, otherwise false.</param>
        /// <returns>the new folder</returns>
        string MoveFile(string source, string destination, bool overrideFile);

        /// <summary>
        /// This method copies a file to a given folder. You can decide whether the file has to be overwritten or not.
        /// If the input is not correct, there will be an exception.
        /// </summary>
        /// <param name="source">The file, has to be full path to file.</param>
        /// <param name="destination">The destination path</param>
        /// <param name="overrideFile">True if yes, otherwise false.</param>
        /// <returns>the new folder</returns>
        string CopyFile(string source, string destination, bool overrideFile);

        /// <summary>
        /// Returns true if the given file allready exists.
        /// </summary>
        /// <param name="fileName">The file to check</param>
        /// <returns>true if the given file allready exists, otherwise false.</returns>
        bool FileExists(string fileName);

        /// <summary>
        /// Creates the complete path to a given filename. Combines the path with tzhe filename.
        /// </summary>
        /// <param name="path"></param>
        /// <param name="fileName"></param>
        /// <returns></returns>
        string GenerateCompleteFilename(string path, string fileName);

        /// <summary>
        /// Deletes the given file.
        /// </summary>
        /// <param name="file"></param>
        /// <returns></returns>
        bool DeleteFile(string file);

        /// <summary>
        /// Returns the directory information for the specified file path.
        /// </summary>
        /// <param name="filePath"></param>
        /// <returns></returns>
        string GetFolderOfFile(string filePath);

        /// <summary>
        /// Returns the extension of the given filepath.
        /// If the path leads to a directory and not a file the result is ''.
        /// </summary>
        /// <param name="fileePath">The filepath to a specific file.</param>
        /// <returns>Returns the extension of the given file.</returns>
        string GetFileExtension(string filePath);

        #endregion

        #region Directory Methods

        /// <summary>
        /// This Method will calculate the Size of a given directory including:
        /// 1- files inside the given Directory
        /// 2- sub directories with their contents(if exist and wanted)
        /// files in the given directory with the files inside the subDirectories will be return 
        /// </summary>
        /// <param name="directory"></param>
        /// <param name="recursive"></param>
        /// <returns></returns>
        long GetDirectorySize(string directory, bool recursive);

        /// <summary>
        /// This Method will calculate the Size of a given directory including:
        /// 1- files inside the given Directory
        /// 2- sub directories with their contents(if exist and wanted)
        /// 3- just calculating specific File Types (if wanted)
        /// files in the given directory with the files inside the subDirectories will be return 
        /// </summary>
        /// <param name="directory"></param>
        /// <param name="recursive"></param>
        /// <param name="fileExtension"></param>
        /// <returns></returns>
        long GetDirectorySize(string directory, bool recursive, string fileExtension);

        /// <summary>
        /// this Method will calculate the Size of a given directory including the size all sub directories and files inside it.
        /// 1- sub directories with their contents(if exist)
        /// 2- files inside the given Directory
        /// </summary>
        /// <param name="directory"></param>
        /// <param name="filesNum"></param>
        /// <param name="subDirNum"></param>
        /// <returns>
        /// The size of the given directory.
        /// </returns>
        float CalculateDirectorySize(string directory, int filesNum, int subDirNum);

        /// <summary>
        /// Returns all files of a given directory.
        /// </summary>
        /// <param name="directoryName"></param>
        /// <returns></returns>
        string[] GetFileNamesOfDirectory(string directoryName);

        /// <summary>
        /// Creates directory and directories of the the path if they don't exist
        /// </summary>
        /// <param name="path"></param>
        /// <returns></returns>
        bool CreateDirectoryIfNotExisting(string path);

        /// <summary>
        /// This method checks whether the given folder path exists.
        /// </summary>
        /// <param name="folder">The expected folder</param>
        /// <returns>
        /// True if exist, otherwise false.
        /// </returns>
        bool IsDirecotry(string folder);

        /// <summary>
        /// Delete the directory with its content.
        /// </summary>
        /// <param name="m_DestinationDirectory"></param>
        void DeleteFolderWithContent(string m_DestinationDirectory);

        /// <summary>
        /// Delete the directory with its content and does not throw if folder not exist.
        /// </summary>
        /// <param name="m_DestinationDirectory">The folder path to delete.</param>
        void DeleteFolderWithContentIfExist(string m_DestinationDirectory);

        #endregion

        #region Application Methods

        /// <summary>
        /// Returns the folder of the application instance.
        /// </summary>
        string GetCurrentApplicationFolder { get; }

        #endregion      
    }
}