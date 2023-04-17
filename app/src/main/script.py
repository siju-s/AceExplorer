# This script moves specific string from source to dest
# TODO Add support for moving multiple strings at once
import os
import shutil

source_folder = "/Volumes/TECH/ANDROID_WS/AceExplorer/feature/appmanager/src/main/res"
destination_folder = "/Volumes/TECH/ANDROID_WS/AceExplorer/common/src/main/res"
search_string = '<string name="package_name">'

for root, dirs, files in os.walk(source_folder):

    for file in files:
        file_path = os.path.join(root, file)
        # print(file_path)
        if os.path.isfile(file_path):
            folder_name = os.path.basename(os.path.dirname(file_path))
            name = os.path.basename(file_path)

            if name == "strings.xml" :
                print("Folder name", folder_name)

                with open(file_path, "r") as file1:
                    lines = file1.readlines()

                for line in lines:
                    if search_string in line:
                        dest_path = destination_folder + os.sep + folder_name
                        print("Dest ",dest_path)
                        str_file = os.path.join(dest_path, name)
                        if os.path.exists(dest_path):
                            # Move the line to the destination folder
                            if not os.path.exists(str_file):
                                with open(os.path.join(dest_path, name), "w+") as dest_file:
                                    dest_file.write("<resources>" + "\n")
                                    dest_file.write(line)
                                    dest_file.write("</resources>")
                            else :
                                with open(os.path.join(dest_path, name), "r") as f:
                                    contents = f.readlines()
                                    contents.insert(len(contents) - 2, line)
                                with open(os.path.join(dest_path, name), "w") as dest_file:
                                    dest_file.writelines(contents)
                        else:
                            os.mkdir(dest_path)
                            with open(os.path.join(dest_path, name), "w+") as dest_file:
                                dest_file.write("<resources>" + "\n")
                                dest_file.write(line)
                                dest_file.write("</resources>")


