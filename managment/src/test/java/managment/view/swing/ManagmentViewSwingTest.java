package managment.view.swing;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.core.matcher.JLabelMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JButtonFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;

import managment.model.Client;

@RunWith(GUITestRunner.class)
public class ManagmentViewSwingTest extends AssertJSwingJUnitTestCase {

	private ManagmentViewSwing managmentViewSwing;

	private FrameFixture window;

	@Override
	protected void onSetUp() {
		GuiActionRunner.execute(() -> {
			managmentViewSwing = new ManagmentViewSwing();
			return managmentViewSwing;
		});
		window = new FrameFixture(robot(), managmentViewSwing);
		window.show();
	}

	@Test
	@GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("Client Name"));
		window.textBox("clientNameBox").requireEnabled();
		window.label(JLabelMatcher.withText("Purchase Ammount"));
		window.textBox("purchaseAmmountBox").requireEnabled();
		window.button(JButtonMatcher.withText("Add New Client")).requireDisabled();
		window.button(JButtonMatcher.withText("Add Ammount")).requireDisabled();
		window.list("clientList");
		window.list("purchaseList");
		window.button(JButtonMatcher.withText("Delete Selected Client")).requireDisabled();
		window.button(JButtonMatcher.withText("Delete Selected Purchase")).requireDisabled();
		window.label(JLabelMatcher.withText(" "));
	}

	@Test
	public void testWhenClientNameIsNonEmptyThenAddNewClientButtonShouldBeEnabled() {
		window.textBox("clientNameBox").enterText("testClient");
		window.button(JButtonMatcher.withText("Add New Client")).requireEnabled();
	}

	@Test
	public void testWhenClientNameIsBlankThenAddNewClientButtonShouldBeDisabled() {
		window.textBox("clientNameBox").enterText(" ");
		window.button(JButtonMatcher.withText("Add New Client")).requireDisabled();
	}

	@Test
	public void testDeleteClientButtonShouldBeEnabledOnlyWhenAClientIsSelected() {
		GuiActionRunner.execute(() -> 
			managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));
		
		window.list("clientList").selectItem(0);
		JButtonFixture deleteButton =
				window.button(JButtonMatcher.withText("Delete Selected Client"));
				deleteButton.requireEnabled();
				window.list("clientList").clearSelection();
				deleteButton.requireDisabled();
	}
	
	

}
