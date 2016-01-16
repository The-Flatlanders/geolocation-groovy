# geolocation-groovy
IDT is starting a valuable package delivery service and has already developed half of the software/hardware solution needed to track all of the valuable packages. IDT has built custom GPS units for each package that will transmit positional data to our network. IDT has also aggregated all of the data for all of the packages in one place since all of the packages report to the same RESTful web service. This program is intended to be the other side: the interface with the user.
#Setting up Eclipse
to install plugins in eclipse, you will need to go to help>install new software.
In the box labeled work with, you will paste links to download sites where the plugins we need are available. The first site you will need to paste in this area is 
http://dist.springsource.org/snapshot/GRECLIPSE/e4.5/
This is the download site for the groovy plugin for eclipse.
Second, you will need the git plugin for eclipse. This is available at 
http://download.eclipse.org/egit/updates 
Once you have both of these plugins installed, you are ready to proceed. 

# Importing the repository into Eclipse
once you have Egit and groovy installed, you will want to be able to see the repository view in the Eclipse editor. To do this, go to Window>Show View>Other>Git>Git Repositories. This should cause a small window to appear below the package explorer with three options inside. Choose the second option, clone a Git repository.
A dialog should appear. In the URI box, paste this link: https://github.com/The-Flatlanders/geolocation-groovy
all of the other boxes should fill themselves. The only other things you have to fill out in this dialog are the Username and Password. These should be the information from your github account. Once you have completed this information, click next, next, then finish. In the Git Repositories window, you should see a new repository named geolocation-groovy [master]. Right click on this repository, and click on import projects. A new dialog should appear. Click next, then finish. Now, you should see the groovy project (called Groovy Package Tracker) in the package explorer. 

#How to use Git
There are three important commands to using git. These are commit, push, and pull. 
commiting tells git to save your place as a checkpoint. This checkpoint can be reverted to at any time, and so it is quite helpful to have these before making changes. You should commit *every time you run the code* if anything has changed. To commit using EGit, right click on the project in the package explorer, and select team>commit. When you press commit, a dialog should appear. It will ask for comments and for you to choose the files you want to mark your progress on. Ideally, you should comment on every commit you make explaining what aspect of the program was changed in that commit. This takes a few extra seconds and has the potential to save you a lot of pain in the long run. Committing is intended to save your personal progress in tracking a bug or working on a particular feature, but on the project scale, miniscule changes to the code are insignificant and unimportant when the team is looking to revert a large feature or aspect of the program. This is where pushing comes in. You should only push your changes when you have completed a major feature of the project. Each push should contain many commits from the stages of development of that feature. Every time you push you are required to first pull from the server to synchronize any changes that were made while you were working.

#How to use servlets
* http://courses.coreservlets.com/Course-Materials/csajsp2.html
* http://stackoverflow.com/questions/16574482/decoding-json-string-in-java
* *http://stackoverflow.com/questions/3028490/calling-a-java-servlet-from-javascript

#How to run the event simulator
* Ensure the web server is already running
* go to the event simulator folder in the project
* run the file labelled Exec
* input the args (simple args are -n *.gpx)

