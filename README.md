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
A dialog should appear. In the URI box, paste this link: 
