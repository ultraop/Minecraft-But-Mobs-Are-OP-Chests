# Resource Pack Status

The resource-pack animation implementation has been retired and removed from the current build.

## Current build

- The Paper plugin uses the original vanilla Mob entity.
- It does not create custom ItemDisplay model halves.
- It does not set custom item model IDs.
- It does not require a client resource pack.
- Remove any old Mob Chests resource pack from the client.

The mob's vanilla entity pitch is animated with AI disabled while the chest is active. This keeps native dimensions and texture and prevents the normal look goals from repeatedly changing the pose. It is a pitch-based approximation, not an independent hinged head/body model. It cannot separately animate every model bone or squid tentacle.

## Custom item appearance

The current build uses vanilla item textures, custom display names and PersistentDataContainer IDs. Golden foods therefore look like their vanilla base items without a resource pack. Emerald Armor uses green-dyed leather armor.

A future custom-texture release would need a separately versioned and tested resource pack; it is not part of this build.
