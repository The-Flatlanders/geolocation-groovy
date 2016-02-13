#Software Requirements and How We Satisfied Them: 

-The solution shall handle multiple simultaneous GPS tracked packages sending updates.

We ran the simulator with up to four packages sending updates at once and the packages were properly displayed on the map.


-The solution shall be easily accessible from a Windows 7 computer. 

This was tested on a Windows 7 computer and worked successfully.


-The solution shall support an admin mode that shows all package location updates on a map.

We logged in with an admin account and all package markers were displayed on the map.

-The solution shall support a user mode that shows a subset of package location updates on a map.

We made user accounts and successfully showed only those the user had access to.


-The solution shall accept a list of UUIDs in user mode to control the subset of package location updates displayed on the map. 

We entered many UUIDs for a user and those UUUID's markers were displayed on the map.


-The solution shall accept name, destination, and GPS unit UUID information as HTTP query parameters on a HTTP GET of the URL path "/tracknewpackage". An example follows: GET http://127.0.0.1:8080/tracknewpackage?name=Some+Name+Here&destinationLat=42.4877185&destinationLon=-71.8249125&uuid=b0f9bb21-160f-4089-ad1c-56ae8b2d5c93 

We ran the event simulator which called the doGet() method with this type of query successfully.


-The solution shall respond with a JSON encoded body which includes the registered uuid on an HTTP GET of the URL path "/tracknewpackage". An example follows: GET Response Body: { "ackUUID":"[b0f9bb21-160f-4089-ad1c-56ae8b2d5c93]" }

We printed out the response of the get request and confirmed that it was indeed correct.


-The solution shall accept a JSON encoded body which includes location, elevation, and time on a HTTP POST to the URL path "/packagetrackupdate/". An example follows: POST http://127.0.0.1:8080/packagetrackupdate/b0f9bb21-160f-4089-ad1c-56ae8b2d5c93 POST Body: {"lat":"42.4879714","lon":"-71.8250924","ele":"195.9","time":"2015-12-08T08:42:33.188-05:00"}

We ran the event simulator which called the doPost() method with this type of query successfully.


-The solution shall accept a JSON encoded body which includes a delivered flag on a HTTP POST to the URL path "/packagetrackupdate/". An example follows: POST http://127.0.0.1:8080/packagetrackupdate/b0f9bb21-160f-4089-ad1c-56ae8b2d5c93  POST Body: {"delivered":"true"} 

We ran a simulated package that finished in under a second to ensure that end behavior of a package functioned as predicted.


-The solution shall calculate and display distance to destination. 

We ran our program that calculates the distance between any two points, of which this requirement is a subset, and checked the answer.


-The solution shall calculate and display estimated arrival time. 

We calculated estimated time and then the actual time the package took and made sure that the numbers were relatively close to each other.


#Product Documentation

##Login page:
If you already have an account, you can simply login and access all of your packages. Note that you must have cookies enabled at all times. If you do not have an account, simply press the Create Account button and enter all information asked of you. You can then go on to the main page, discussed in detail below.

##Adding a package to the map:
Navigate to the UUID tab by clicking on the tab labeled UUID on the upper left. It is also the default tab when you first access the map page. Then, enter into the text box labeled "Add UUID" the UUID associated with your package. This is just a long string of letters and numbers unique to every package. After you press submit your package will be easily viewed on the map to the right.
Clicking on "Refresh Map" will refresh all of your packages. This will update their positions on the map and their details in the details tab, described more later. Clicking on "Help" brings up this menu.

##Details Tab:
When you first navigate to the details tab, you may be dismayed because it is empty. The key is that the tab remains blank until you click on a package. After you click on a package, henceforth referred to as the active package, you can see all of its information in the details tab. This includes aspects of the active package like its location, destination, and Estimated Time of Arrival (ETA). You will also see a notes section. This is a place that you can add notes to remind yourself about the package. Notes you add will be visible by anyone else who has access to the UUID, usually just you and the package administrator. This therefore serves as a great and easy way to communicate with IDT. 

##Map Help:
Clicking on a package places focus on it, turning it into the current active package. The map automatically zooms in to focus on the package, its destination point, and its start point, conveniently marked with pins. A green pin signifies the end while a blue pin signifies the beginning. You will also notice that an active package has a line leading back from it to its starting point. This shows the path the package took with as much accuracy as possible. 


#Software Documentation

##geolocation-groovy
IDT is starting a valuable package delivery service and has already developed half of the software/hardware solution needed to track all of the valuable packages. IDT has built custom GPS units for each package that will transmit positional data to our network. IDT has also aggregated all of the data for all of the packages in one place since all of the packages report to the same RESTful web service. This program is intended to be the other side: the interface with the user.

##Setting up Eclipse
To install plugins in eclipse, you will need to go to help>install new software.
In the box labeled work with, you will paste links to download sites where the plugins we need are available. The first site you will need to paste in this area is 
http://dist.springsource.org/snapshot/GRECLIPSE/e4.5/
This is the download site for the groovy plugin for eclipse.
Second, you will need the git plugin for eclipse. This is available at 
http://download.eclipse.org/egit/updates 
Once you have both of these plugins installed, you are ready to proceed. 

##Importing the repository into Eclipse
Once you have Egit and groovy installed, you will want to be able to see the repository view in the Eclipse editor. To do this, go to Window>Show View>Other>Git>Git Repositories. This should cause a small window to appear below the package explorer with three options inside. Choose the second option, clone a Git repository.
A dialog should appear. In the URI box, paste this link: https://github.com/The-Flatlanders/geolocation-groovy
all of the other boxes should fill themselves. The only other things you have to fill out in this dialog are the Username and Password. These should be the information from your github account. Once you have completed this information, click next, next, then finish. In the Git Repositories window, you should see a new repository named geolocation-groovy [master]. Right click on this repository, and click on import projects. A new dialog should appear. Click next, then finish. Now, you should see the groovy project (called Groovy Package Tracker) in the package explorer. 


