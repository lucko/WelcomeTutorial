package me.lucko.welcometutorial;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class Tutorial {
    private final String name;
    private final String requiredRegion;

    @Getter(AccessLevel.NONE)
    private final List<TutorialStage> stages = new ArrayList<>();

    public void addStage(TutorialStage stage) {
        stages.add(stage);
    }

    public List<TutorialStage> getStages() {
        return ImmutableList.copyOf(stages);
    }

}
