using System;
using System.IO;
using System.Text;
using System.Collections.Generic;

class DirectoryTree
{
    static int Main(string[] args)
    {
        try
        {
            // The folder where the EXE resides
            var rootPath = AppContext.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar);
            var outFile = Path.Combine(rootPath, "struktur.txt");

            // Build tree
            var sb = new StringBuilder();
            sb.AppendLine(Path.GetFileName(rootPath) + Path.DirectorySeparatorChar);

            try
            {
                var root = new DirectoryInfo(rootPath);
                // first level entries excluding the exe itself and the output file
                var entries = new List<FileSystemInfo>();
                entries.AddRange(root.GetDirectories());
                entries.AddRange(root.GetFiles());

                // Sort: directories first, then files, both alphabetically
                entries.Sort((a, b) =>
                {
                    int type = (a is DirectoryInfo ? 0 : 1).CompareTo(b is DirectoryInfo ? 0 : 1);
                    if (type != 0) return type;
                    return StringComparer.OrdinalIgnoreCase.Compare(a.Name, b.Name);
                });

                for (int i = 0; i < entries.Count; i++)
                {
                    bool isLast = i == entries.Count - 1;
                    WriteEntry(entries[i], sb, prefix: "", isLast: isLast, rootPath: rootPath, ignoreNames: new HashSet<string>(StringComparer.OrdinalIgnoreCase)
                    {
                        Path.GetFileName(System.Diagnostics.Process.GetCurrentProcess().MainModule?.FileName ?? ""),
                        Path.GetFileName(outFile)
                    });
                }
            }
            catch (Exception ex)
            {
                sb.AppendLine($"[Fel vid läsning av rotmapp: {ex.Message}]");
            }

            // Write to file in UTF-8 without BOM
            File.WriteAllText(outFile, sb.ToString(), new UTF8Encoding(encoderShouldEmitUTF8Identifier:false));
            return 0;
        }
        catch (Exception ex)
        {
            try
            {
                File.WriteAllText(Path.Combine(AppContext.BaseDirectory, "struktur-error.txt"),
                    $"Ett fel inträffade: {ex}",
                    new UTF8Encoding(false));
            }
            catch { /* ignore */ }
            return 1;
        }
    }

    static void WriteEntry(FileSystemInfo entry, StringBuilder sb, string prefix, bool isLast, string rootPath, HashSet<string> ignoreNames)
    {
        string connector = isLast ? "└── " : "├── ";
        if (entry is DirectoryInfo dir)
        {
            sb.AppendLine(prefix + connector + dir.Name + Path.DirectorySeparatorChar);
            string nextPrefix = prefix + (isLast ? "    " : "│   ");
            WriteDirectory(dir, sb, nextPrefix, rootPath, ignoreNames);
        }
        else if (entry is FileInfo file)
        {
            if (!ignoreNames.Contains(file.Name))
                sb.AppendLine(prefix + connector + file.Name);
        }
    }

    static void WriteDirectory(DirectoryInfo dir, StringBuilder sb, string prefix, string rootPath, HashSet<string> ignoreNames)
    {
        FileSystemInfo[] children;
        try
        {
            children = dir.GetFileSystemInfos();
        }
        catch (UnauthorizedAccessException)
        {
            sb.AppendLine(prefix + "└── [Åtkomst nekad]");
            return;
        }
        catch (Exception ex)
        {
            sb.AppendLine(prefix + $"└── [Fel: {ex.Message}]");
            return;
        }

        var list = new List<FileSystemInfo>(children);
        // Sort: directories first, then files, both alphabetically
        list.Sort((a, b) =>
        {
            int type = (a is DirectoryInfo ? 0 : 1).CompareTo(b is DirectoryInfo ? 0 : 1);
            if (type != 0) return type;
            return StringComparer.OrdinalIgnoreCase.Compare(a.Name, b.Name);
        });

        for (int i = 0; i < list.Count; i++)
        {
            bool isLast = i == list.Count - 1;
            WriteEntry(list[i], sb, prefix, isLast, rootPath, ignoreNames);
        }
    }
}