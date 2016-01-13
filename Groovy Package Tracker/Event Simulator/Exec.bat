set /p args=Enter Params
%~d0
cd %~p0
cd Util
GRE\bin\groovy PackageEventsSimulator.groovy %args%
