package managment.view.swing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import javax.swing.DefaultListModel;

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
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.inOrder;
import static java.time.LocalDateTime.now;

import managment.controller.ManagmentController;
import managment.model.Client;
import managment.model.Purchase;

@RunWith(GUITestRunner.class)
public class ManagmentViewSwingTest extends AssertJSwingJUnitTestCase {

	private static final LocalDateTime TEST_DATE = LocalDate.of(2024, Month.JANUARY, 1).atStartOfDay();

	@Mock
	private ManagmentController managmentController;
	
	private ManagmentViewSwing managmentViewSwing;

	private FrameFixture window;
	
	private AutoCloseable closeable;

	@Override
	protected void onSetUp() {
		closeable = MockitoAnnotations.openMocks(this);
		GuiActionRunner.execute(() -> {
			managmentViewSwing = new ManagmentViewSwing();
			managmentViewSwing.setManagmentController(managmentController);
			return managmentViewSwing;
		});
		window = new FrameFixture(robot(), managmentViewSwing);
		window.show();
	}
	
	@Override
	protected void onTearDown() throws Exception {
	closeable.close();
	}
	
	@Test
	@GUITest
	public void testControlsInitialStates() {
		window.label(JLabelMatcher.withText("Client Name"));
		window.textBox("clientNameBox").requireEnabled();
		window.label(JLabelMatcher.withText("Purchase Amount"));
		window.textBox("purchaseAmountBox").requireEnabled();

		window.button(JButtonMatcher.withText("Add New Client")).requireDisabled();
		window.button(JButtonMatcher.withText("Add Amount")).requireDisabled();

		window.list("clientList");
		window.list("purchaseList");

		window.button(JButtonMatcher.withText("Delete Selected Client")).requireDisabled();
		window.button(JButtonMatcher.withText("Delete Selected Purchase")).requireDisabled();
		window.label("messageLable").requireText(" ");
	}

	@Test
	public void testWhenClientNameIsNonEmptyThenAddNewClientButtonShouldBeEnabled() {
		window.textBox("clientNameBox").enterText("testClient");
		window.button(JButtonMatcher.withText("Add New Client")).requireEnabled();

		window.textBox("clientNameBox").setText("");
		window.textBox("clientNameBox").enterText(" ");
		window.button(JButtonMatcher.withText("Add New Client")).requireDisabled();
	}

	@Test
	public void testDeleteClientButtonShouldBeEnabledOnlyWhenAClientIsSelected() {
		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));

		window.list("clientList").selectItem(0);
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected Client"));
		deleteButton.requireEnabled();
		window.list("clientList").clearSelection();
		deleteButton.requireDisabled();
	}

	@Test
	public void testWhenPurchaseAmountIsNonEmptyAndClientIsSelectedThenAddPurchaseButtonShouldBeEnabled() {
		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));
		window.list("clientList").selectItem(0);

		window.textBox("purchaseAmountBox").enterText("10.0");
		window.button(JButtonMatcher.withText("Add Amount")).requireEnabled();

		window.textBox("purchaseAmountBox").setText("");
		window.textBox("purchaseAmountBox").enterText(" ");
		window.button(JButtonMatcher.withText("Add Amount")).requireDisabled();

		window.list("clientList").clearSelection();
		window.button(JButtonMatcher.withText("Add Amount")).requireDisabled();

		window.textBox("purchaseAmountBox").enterText("10.0");
		window.button(JButtonMatcher.withText("Add Amount")).requireDisabled();

		window.list("clientList").selectItem(0);
		window.button(JButtonMatcher.withText("Add Amount")).requireEnabled();

	}
	
	@Test
	public void testWhenPurchaseAmountIsNotANumberShouldReportError() {
		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));
		window.list("clientList").selectItem(0);

		window.textBox("purchaseAmountBox").enterText("notValidAmount");
		window.button(JButtonMatcher.withText("Add Amount")).click();
		window.label("messageLable").requireText("Amount must be a number");
		
	}

	@Test
	public void testDeletePurchaseButtonShouldBeDisabledWhenAClientAndAPurchaseIsNotSelected() {
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected Purchase"));

		deleteButton.requireDisabled();

		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));

		window.list("clientList").selectItem(0);
		deleteButton.requireDisabled();

		GuiActionRunner
				.execute(() -> managmentViewSwing.getListPurchaseModel().addElement(new Purchase(1, TEST_DATE, 10.0)));

		window.list("clientList").clearSelection();
		
		GuiActionRunner
		.execute(() -> managmentViewSwing.getListPurchaseModel().addElement(new Purchase(1, TEST_DATE, 10.0)));

		window.list("purchaseList").selectItem(0);
		deleteButton.requireDisabled();

	}
	
	@Test @GUITest
	public void testDeletePurchaseButtonShouldBeEnableWhenClientAndAPurchaseAreSelected(){
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected Purchase"));
		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));

		window.list("clientList").selectItem(0);
		GuiActionRunner
				.execute(() -> managmentViewSwing.getListPurchaseModel().addElement(new Purchase(1, TEST_DATE, 10.0)));
		window.list("purchaseList").selectItem(0);
		deleteButton.requireEnabled();
	}

	@Test
	public void testDeletePurchaseButtonShouldBeEnabledWhenAClientAndAPurchaseIsSelected() {
		JButtonFixture deleteButton = window.button(JButtonMatcher.withText("Delete Selected Purchase"));

		GuiActionRunner.execute(() -> managmentViewSwing.getListClientsModel().addElement(new Client(1, "testClient")));
		GuiActionRunner
				.execute(() -> managmentViewSwing.getListPurchaseModel().addElement(new Purchase(TEST_DATE, 10.0)));

		window.list("clientList").selectItem(0);
		window.list("purchaseList").selectItem(0);
		deleteButton.requireEnabled();
	}

	@Test
	public void testShowAllClientsShouldAddClientDescriptionsToTheList() {
		Client client1 = new Client(1, "client1");
		Client client2 = new Client(2, "client2");

		GuiActionRunner.execute(() -> managmentViewSwing.showAllClients(asList(client1, client2)));

		String[] listContents = window.list("clientList").contents();
		assertThat(listContents).containsExactly(client1.toString(), client2.toString());
	}

	@Test
	public void testShowClientNotFoundErrorShouldShowMessageInMessageLable() {
		Client client = new Client(1, "client");
		GuiActionRunner.execute(
				() -> managmentViewSwing.showClientNotFoundError("Client [id=1, name=client] not found", client));
		window.label("messageLable").requireText("Client [id=1, name=client] not found");
	}

	@Test
	public void testClientAddedShouldAddTheClientToTheListAndResetTheMessageLabel() {
		Client client = new Client(1, "client");
		GuiActionRunner.execute(() -> managmentViewSwing.clientAdded(new Client(1, "client")));
		String[] listContents = window.list("clientList").contents();
		assertThat(listContents).containsExactly(client.toString());
		window.label("messageLable").requireText(" ");
	}

	@Test
	public void testClientRemovedShouldRemoveTheClientFromTheListAndResetTheMessageLabel() {
		Client toRemove = new Client(1, "toRemove");
		Client client = new Client(2, "client");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientModel = managmentViewSwing.getListClientsModel();
			listClientModel.addElement(toRemove);
			listClientModel.addElement(client);
		});

		GuiActionRunner.execute(() -> managmentViewSwing.clientRemoved(new Client(1, "toRemove")));

		String[] listContents = window.list("clientList").contents();
		assertThat(listContents).containsExactly(client.toString());
		window.label("messageLable").requireText(" ");
	}

	@Test
	public void testShowAllPurchaseShouldAddPurchaseDescriptionsToTheList() {
		Purchase purchase1 = new Purchase(1, TEST_DATE, 10.0);
		Purchase purchase2 = new Purchase(2, TEST_DATE, 5.0);

		GuiActionRunner.execute(() -> managmentViewSwing.showAllPurchases(asList(purchase1, purchase2)));

		String[] listContents = window.list("purchaseList").contents();
		assertThat(listContents).containsExactly(purchase1.toString(), purchase2.toString());
	}
	
	@Test
	public void testShowPurchaseNotFoundErrorShouldShowMessageInMessageLable() {
		Purchase purchase = new Purchase(1, TEST_DATE, 10.0);
		GuiActionRunner.execute(
				() -> managmentViewSwing.showPurchaseNotFoundError(" ", purchase));
		window.label("messageLable").requireText("Purchase [id=1, orderDate=2024-01-01T00:00, amount=10.0] not found");
	}
	
	@Test
	public void testPurchaseAddedShouldAddThePurchaseToTheListAndResetTheMessageLabel() {
		Purchase purchase = new Purchase(1, TEST_DATE, 10.0);
		GuiActionRunner.execute(() -> managmentViewSwing.purchaseAdded(new Purchase(1, TEST_DATE, 10.0)));
		String[] listContents = window.list("purchaseList").contents();
		assertThat(listContents).containsExactly(purchase.toString());
		window.label("messageLable").requireText(" ");
	}
	
	@Test
	public void testPurchaseRemovedShouldRemoveThePurchaseFromTheListAndResetTheMessageLabel() {
		Purchase toRemove = new Purchase(1, TEST_DATE, 10.0);
		Purchase purchase = new Purchase(2, TEST_DATE, 5.0);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Purchase> listPurchaseModel = managmentViewSwing.getListPurchaseModel();
			listPurchaseModel.addElement(toRemove);
			listPurchaseModel.addElement(purchase);
		});

		GuiActionRunner.execute(() -> managmentViewSwing.purchaseRemoved(new Purchase(1, TEST_DATE, 10.0)));

		String[] listContents = window.list("purchaseList").contents();
		assertThat(listContents).containsExactly(purchase.toString());
		window.label("messageLable").requireText(" ");
	}
	
	@Test
	public void testAddNewClientShouldDelegateToManagmentControllerAdd(){
		window.textBox("clientNameBox").enterText("testClient");
		window.button(JButtonMatcher.withText("Add New Client")).click();
		verify(managmentController).add(new Client("testClient"));
	}
	
	@Test
	public void testDeleteClientShouldDelegateToManagmentControllerRemove(){
		Client toRemove = new Client(1, "toRemove");
		Client client = new Client(2, "client");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientModel = managmentViewSwing.getListClientsModel();
			listClientModel.addElement(toRemove);
			listClientModel.addElement(client);
		});

		window.list("clientList").selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected Client")).click();
		verify(managmentController).remove(toRemove);
	}
	
	@Test
	public void testSelectingClientShouldDelegateToManagmentControllerFindAllPurchasesOf(){
		Client client = new Client(1, "client");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientModel = managmentViewSwing.getListClientsModel();
			listClientModel.addElement(client);
		});

		window.list("clientList").selectItem(0);
		verify(managmentController).findAllPurchasesOf(client);
	}
	
	@Test
	public void testAddPurchaseShouldDelegateToManagmentControllerAdd(){
		Client client = new Client(1, "client");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientModel = managmentViewSwing.getListClientsModel();
			listClientModel.addElement(client);
		});

		window.list("clientList").selectItem(0);
		window.textBox("purchaseAmountBox").enterText("10.0");
		window.button(JButtonMatcher.withText("Add Amount")).click();		
		verify(managmentController).addPurchaseToSelectedClient(client,
				new Purchase(getCurrentDate(), 10.0));
		verify(managmentController).findAllPurchasesOf(client);
		verifyNoMoreInteractions(managmentController);
	}
	
	@Test
	public void testDeleteSelectedPurchaseButtonShouldDelegateToManagmentControllerRemove()	{
		Client client = new Client(1, "client");
		GuiActionRunner.execute(() -> {
			DefaultListModel<Client> listClientModel = managmentViewSwing.getListClientsModel();
			listClientModel.addElement(client);
		});
		window.list("clientList").selectItem(0);
		Purchase toRemove = new Purchase(1, getCurrentDate(), 10.0);
		GuiActionRunner.execute(() -> {
			DefaultListModel<Purchase> listPurchaseModel = managmentViewSwing.getListPurchaseModel();
			listPurchaseModel.addElement(toRemove);
		});
		window.list("purchaseList").selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected Purchase")).click();
		InOrder inOrder = inOrder(managmentController);
		inOrder.verify(managmentController).remove(toRemove);
	}
	
	
	private LocalDateTime getCurrentDate() {
		return LocalDateTime.of(
				now().getYear(),
				now().getMonth(),
				now().getDayOfMonth(),
				now().getHour(),
				now().getMinute());
	}
	
}

