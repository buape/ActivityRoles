<h1 align="center">ActivityRoles</h1>

<p align="center">

![Modrinth Downloads](https://img.shields.io/modrinth/dt/activityroles?style=for-the-badge)
![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/activityroles?style=for-the-badge)
![Modrinth Version](https://img.shields.io/modrinth/v/activityroles?style=for-the-badge)

</p>

ActivityRoles is a simple plugin that allows you to grant players roles in your Discord server based on their in-game play time.

This plugin uses [DiscordSRV](https://modrinth.com/plugin/discordsrv) as the linking system between your Minecraft server and Discord server, so make sure that you have it installed and setup using the `linking.yml` file in DiscordSRV's configuration.

There are two types of syncs:

-   Giving a player a role based on how long they have ever played on the server. (type: "total")
-   Giving a player a role based on how recently they have played on the server. (type: "seen")

You can list times in either minutes, hours, days, or weeks, by using "m", "h", "d", or "w"

NOTE: Offline players will only have their "seen" roles synced, they must be online to have their "total" role synced. This is a MC limitation.
