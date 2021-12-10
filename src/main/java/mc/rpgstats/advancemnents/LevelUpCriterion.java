package mc.rpgstats.advancemnents;

import com.google.gson.JsonObject;
import mc.rpgstats.main.CustomComponents;
import mc.rpgstats.main.RPGStats;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.advancement.criterion.AbstractCriterionConditions;
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer;
import net.minecraft.predicate.entity.AdvancementEntityPredicateSerializer;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class LevelUpCriterion extends AbstractCriterion<LevelUpCriterion.LevelCriteria> {
    public static final Identifier ID = new Identifier(RPGStats.MOD_ID, "player_level");
    private static final Identifier ANY_ID = new Identifier(RPGStats.MOD_ID, "_any");
    
    @Override
    protected LevelCriteria conditionsFromJson(JsonObject obj, EntityPredicate.Extended pred, AdvancementEntityPredicateDeserializer predicateDeserializer) {
        return new LevelCriteria(pred, obj.get("level").getAsInt(), obj.get("stat").getAsString());
    }
    
    @Override
    public Identifier getId() {
        return ID;
    }
    
    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, (levelCriteria) -> levelCriteria.matches(player));
    }
    
    public static class LevelCriteria extends AbstractCriterionConditions {
        private final int level;
        private final Identifier id;
        
        public LevelCriteria(EntityPredicate.Extended playerPredicate, int lvl, String id) {
            super(ID, playerPredicate);
            this.level = lvl;
            this.id = new Identifier(id);
        }
        
        public boolean matches(ServerPlayerEntity player) {
            if (id.equals(ANY_ID)) {
                return RPGStats.getHighestLevel(player) >= level;
            } else {
                return CustomComponents.STATS.get(player).getOrCreateID(id).getLevel() >= level;
            }
        }
        
        @Override
        public JsonObject toJson(AdvancementEntityPredicateSerializer ser) {
            JsonObject jsonObject = super.toJson(ser);
            jsonObject.addProperty("level", level);
            jsonObject.addProperty("stat", id.toString());
            return jsonObject;
        }
    }
}
