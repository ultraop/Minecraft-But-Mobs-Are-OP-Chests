# Resource Pack Status

The resource-pack animation implementation has been retired from the current build.

## Current build

- The Paper plugin uses the original vanilla Mob entity.
- It does not create custom ItemDisplay model halves.
- It does not set custom item model IDs.
- It does not require a client resource pack.
- Disable/remove any older UltraOP Mob Chests resource pack from the client.

The mob's vanilla entity pitch is animated while body yaw is held. This keeps native dimensions and texture, but is only a head/front-pose approximation. It cannot independently animate every model bone or squid tentacle.

## Custom item appearance

The current build uses vanilla item textures, custom display names and PersistentDataContainer IDs. Golden foods will therefore look like their vanilla base items without a resource pack. Emerald Armor uses green-dyed leather armor.

A future custom-texture version would need a separately versioned and tested resource pack again; it is not part of this build.
