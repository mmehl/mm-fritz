# mm.fritz

A little program to enable and disable filter rules in Fritz!Box.

The idea is:
* my son can disable the "Minecraft" filter, which blocks the ports to the Minecraft servers.
* the button can be pressed once a day (button press is logged to a database).
* the filter is automatically disabled after half an hour

The first try was with TR064 - API ([FritzT064]), but this does not yet support changing filter rules.
AVM Support told me they might add it sometime.

The new solution is simulating the UI with htmlunit.

The program has the following parts:
- Authentication: check via LDAP if the user is allowed to use this function
- Database: log tried and successful to a mysql database
- EnableDisableMinecraft: the htmlunit code to simulate login and changing filters
- WebApp with index.html: a little servlet and html5 page as UI and controller 

## Dependencies

see ivy.xml

## Resources

* [AVM API Description](http://avm.de/service/schnittstellen/) (German)
* [FritzT064](https://github.com/mirthas/FritzTR064/tree/master/examples)
