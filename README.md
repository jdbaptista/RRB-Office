# RRB-Office
This program was written for RRB Construction in 2021, and was ported to this repo later. Original commits are in a private repo.

This is business software for small construction companies. If you plan to use this in production, be sure to know that this code is not well tested nor has any internal testing whatsoever. The testing that has been done was through manager scrutiny of numbers. The program's intended use is in quickly generating reports that are then confirmed by a manager. Testing was not implemented in the interest of development time under the assumption that numbers would be checked by the manager. I'm not responsible for any issues arising from use of this program.

That said, this program quickly generates nicely printable excel workbooks filled with reports about client and material costs which is useful in rough estimates of billing and analysis of project effectiveness. Take a look at the files folder for example input and output.

EXAMPLE OUTPUT:

![jobsite1](https://user-images.githubusercontent.com/18275346/210695499-0ea66353-48ca-43c9-9315-1dd3d727841e.png)
![jobsite_total_1](https://user-images.githubusercontent.com/18275346/210695505-0479cc98-7c7b-41bf-81a5-8324df064706.png)


This program was written in Java and Apache POI, and as such requires Java 17+ to be downloaded to your computer. I have hardcoded the file locations relative to the .jar file, so be sure not to move input files around, and download the program to a folder that is not reserved by the OS (ex. downloads, programs). To change the file locations, refer to the GUI controller.

Feel free to reach out if there are any questions :).
