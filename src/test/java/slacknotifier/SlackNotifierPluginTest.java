package slacknotifier;

import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.CONFIG;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.ENABLED;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.HOOK;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.INCLUDE_BRANCH;
import static com.koant.sonar.slacknotifier.common.SlackNotifierProp.USER;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.koant.sonar.slacknotifier.SlackNotifierPlugin;
import com.koant.sonar.slacknotifier.extension.task.SlackPostProjectAnalysisTask;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SlackNotifierPluginTest {

    private SlackNotifierPlugin plugin = new SlackNotifierPlugin();

    @Test
    public void define_expectedExtensionsAdded() {

        Plugin.Context mockContext = mock(Plugin.Context.class);
        plugin.define(mockContext);
        ArgumentCaptor<List> arg = ArgumentCaptor.forClass(List.class);
        verify(mockContext, times(1)).addExtensions(arg.capture());

        List extensions = arg.getValue();
        Assert.assertEquals(6, extensions.size());
        Assert.assertEquals(HOOK.property(), ((PropertyDefinition) extensions.get(0)).key());
        Assert.assertEquals(USER.property(), ((PropertyDefinition) extensions.get(1)).key());
        Assert.assertEquals(ENABLED.property(), ((PropertyDefinition) extensions.get(2)).key());
        Assert.assertEquals(INCLUDE_BRANCH.property(),
            ((PropertyDefinition) extensions.get(3)).key());
        Assert.assertEquals(CONFIG.property(), ((PropertyDefinition) extensions.get(4)).key());
        Assert.assertEquals(SlackPostProjectAnalysisTask.class, extensions.get(5));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void define_noDupliacteIndexes() {

        Plugin.Context mockContext = mock(Plugin.Context.class);
        plugin.define(mockContext);
        ArgumentCaptor<List> arg = ArgumentCaptor.forClass(List.class);
        verify(mockContext, times(1)).addExtensions(arg.capture());

        List<Object> extensions = arg.getValue();

        Set<Integer> indexes = extensions.stream().filter(PropertyDefinition.class::isInstance)
            .map(PropertyDefinition.class::cast).map(PropertyDefinition::index).
                collect(Collectors.toSet());
        Assert.assertEquals(5, indexes.size());

    }

}
