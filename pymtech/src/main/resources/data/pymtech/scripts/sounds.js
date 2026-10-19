eventManager.on('abilityEnabled', function(e) {
    if(e.getAbility().getAdditionalNbtData().getBoolean('pymtech:helmet_sound')) {
        var helmet = e.getLivingEntity().getItemInSlot('head').getNbtTag().getInt('Opening');
        e.getWorld().playSound(helmet === 0 ? 'pymtech:helmet_opening' : 'pymtech:helmet_closing', 'player', e.getEntity().getPosX(), e.getEntity().getPosY() + e.getEntity().getHeight() / 2, e.getEntity().getPosZ(), 1, 1);
    }

    if(e.getAbility().getAdditionalNbtData().getBoolean('pymtech:button_sound')) {
        e.getWorld().playSound('pymtech:button', 'player', e.getEntity().getPosX(), e.getEntity().getPosY() + e.getEntity().getHeight() / 2, e.getEntity().getPosZ(), 1, 1);
    }

    if(e.getAbility().getAdditionalNbtData().getBoolean('pymtech:blaster_sound')) {
        e.getWorld().playSound('pymtech:stinger_blaster', 'player', e.getEntity().getPosX(), e.getEntity().getPosY() + e.getEntity().getHeight() / 2, e.getEntity().getPosZ(), 1, 1);
    }
});