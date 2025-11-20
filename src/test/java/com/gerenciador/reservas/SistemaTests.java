package com.gerenciador.reservas;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.Select;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.time.Duration;
import java.util.List;

public class SistemaTests {

	private WebDriver driver;
	private WebDriverWait wait;
	private final String baseUrl = "http://localhost:8080";

	@BeforeEach
	public void setUp() {
		WebDriverManager.firefoxdriver().setup();
		driver = new FirefoxDriver();
		wait = new WebDriverWait(driver, Duration.ofSeconds(10));
	}

	@AfterEach
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
	}

	/**
	 * Método auxiliar para verificar a validação HTML5 'required' (valueMissing).
	 */
	private Boolean isCampoInvalido(WebElement elemento) {
		JavascriptExecutor js = (JavascriptExecutor) driver;
		return (Boolean) js.executeScript("return arguments[0].validity.valueMissing;", elemento);
	}

	@Test
	public void CT01_CadastrarNovaSala() {
		driver.get(baseUrl + "/");
		wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Gerenciar Salas"))).click();
		wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Adicionar Nova Sala"))).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));

		String salaId = "S" + (int)(Math.random() * 10000);
		String salaNome = "Sala de Conferência " + salaId;
		String salaCapacidade = "25";

		driver.findElement(By.id("id")).sendKeys(salaId);
		driver.findElement(By.id("nome")).sendKeys(salaNome);
		WebElement capacidadeInput = driver.findElement(By.id("capacidade"));
		capacidadeInput.clear();
		capacidadeInput.sendKeys(salaCapacidade);

		driver.findElement(By.xpath("//button[text()='Salvar']")).click();

		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciamento de Salas"));
		WebElement msgSucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("sucesso")));
		assertTrue(msgSucesso.getText().contains("sucesso"));

		String xpathBusca = String.format(
				"//tr[td[normalize-space()='%s'] and td[normalize-space()='%s'] and td[normalize-space()='%s']]",
				salaId, salaNome, salaCapacidade
		);
		WebElement novaSalaNaTabela = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathBusca)));
		assertNotNull(novaSalaNaTabela, "A nova sala não foi encontrada na tabela após o cadastro.");
	}

	@Test
	public void CT02_AtualizarSalaQuandoIdDuplicado() {
		// --- 1. Dados de Teste ---
		String idDuplicado = "S_DUP_" + (int)(Math.random() * 10000);

		String nomeOriginal = "Sala Original";
		String capacidadeOriginal = "10";

		String nomeAtualizado = "Sala Atualizada via ID";
		String capacidadeAtualizada = "99";

		// --- 2. Setup: Cadastrar a sala original ---
		driver.get(baseUrl + "/salas/nova");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));
		driver.findElement(By.id("id")).sendKeys(idDuplicado);
		driver.findElement(By.id("nome")).sendKeys(nomeOriginal);
		WebElement capInput = driver.findElement(By.id("capacidade"));
		capInput.clear();
		capInput.sendKeys(capacidadeOriginal);
		driver.findElement(By.xpath("//button[text()='Salvar']")).click();

		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciamento de Salas"));

		// --- 3. Ação: Tentar cadastrar com o MESMO ID mas dados DIFERENTES ---
		wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Adicionar Nova Sala"))).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));

		driver.findElement(By.id("id")).sendKeys(idDuplicado); // Mesmo ID
		driver.findElement(By.id("nome")).sendKeys(nomeAtualizado); // Nome diferente
		WebElement capInput2 = driver.findElement(By.id("capacidade"));
		capInput2.clear();
		capInput2.sendKeys(capacidadeAtualizada); // Capacidade diferente

		driver.findElement(By.xpath("//button[text()='Salvar']")).click();

		// --- 4. Verificação ---
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciamento de Salas"));
		WebElement msgSucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("sucesso")));
		assertTrue(msgSucesso.isDisplayed(), "A mensagem de sucesso não apareceu após a atualização.");

		String xpathAtualizado = String.format(
				"//tr[td[normalize-space()='%s'] and td[normalize-space()='%s'] and td[normalize-space()='%s']]",
				idDuplicado, nomeAtualizado, capacidadeAtualizada
		);

		WebElement salaAtualizada = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathAtualizado)));
		assertNotNull(salaAtualizada, "A sala não foi atualizada com os novos dados na tabela.");

		String xpathAntigo = String.format(
				"//tr[td[normalize-space()='%s'] and td[normalize-space()='%s']]",
				idDuplicado, nomeOriginal
		);
		List<WebElement> antigos = driver.findElements(By.xpath(xpathAntigo));
		assertTrue(antigos.isEmpty(), "A sala antiga ainda está visível na tabela (duplicidade?).");
	}

	@Test
	public void CT03_ValidacaoCamposObrigatoriosVazios() {
		driver.get(baseUrl + "/salas/nova");
		WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
		assertEquals("Nova Sala", h1.getText());
		WebElement salvarButton = driver.findElement(By.xpath("//button[text()='Salvar']"));

		salvarButton.click();
		WebElement idInput = driver.findElement(By.id("id"));
		assertTrue(isCampoInvalido(idInput), "O campo 'id' não foi marcado como inválido (valueMissing).");

		idInput.sendKeys("ID-VALIDO");
		salvarButton.click();
		WebElement nomeInput = driver.findElement(By.id("nome"));
		assertTrue(isCampoInvalido(nomeInput), "O campo 'nome' não foi marcado como inválido (valueMissing).");

		nomeInput.sendKeys("NOME-VALIDO");
		driver.findElement(By.id("capacidade")).clear();
		salvarButton.click();
		WebElement capacidadeInput = driver.findElement(By.id("capacidade"));
		assertTrue(isCampoInvalido(capacidadeInput), "O campo 'capacidade' não foi marcado como inválido (valueMissing).");
	}

	@Test
	public void CT04_CadastrarNovoEquipamento() {
		driver.get(baseUrl + "/");
		wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Gerenciar Equipamentos"))).click();
		wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Adicionar Novo Equipamento"))).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nome")));

		String nomeEquipamento = "Flip Chart";
		String descEquipamento = "Quadro branco com bloco de papel";

		driver.findElement(By.id("nome")).sendKeys(nomeEquipamento);
		driver.findElement(By.id("descricao")).sendKeys(descEquipamento);
		driver.findElement(By.xpath("//button[text()='Salvar']")).click();

		// --- Verificação ---
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciamento de Equipamentos"));
		WebElement msgSucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("sucesso")));
		assertTrue(msgSucesso.isDisplayed(), "A mensagem de sucesso não foi exibida.");

		String xpathBusca = String.format(
				"//tr[td[normalize-space()='%s'] and td[normalize-space()='%s']]",
				nomeEquipamento, descEquipamento
		);
		WebElement novoEquipamentoNaTabela = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathBusca)));
		assertNotNull(novoEquipamentoNaTabela, "O novo equipamento não foi encontrado na tabela.");
	}

	@Test
	public void CT05_AssociarEquipamentoASala() {
		// --- Dados de Teste Únicos ---
		String nomeEquipamento = "Projetor Associação";
		String descEquipamento = "Equipamento Associação";
		String salaId = "T02";
		String nomeSala = "Sala para Associação";
		String capacidadeSala = "5";

		// --- 1. SETUP 1: Cadastrar o Equipamento ---
		driver.get(baseUrl + "/equipamentos/novo");
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Novo Equipamento"));
		driver.findElement(By.id("nome")).sendKeys(nomeEquipamento);
		driver.findElement(By.id("descricao")).sendKeys(descEquipamento);
		driver.findElement(By.xpath("//button[text()='Salvar']")).click();
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciamento de Equipamentos"));

		// --- 2. SETUP 2: Cadastrar a Sala (sem equipamento) ---
		driver.get(baseUrl + "/salas/nova");
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Nova Sala"));
		driver.findElement(By.id("id")).sendKeys(salaId);
		driver.findElement(By.id("nome")).sendKeys(nomeSala);
		WebElement capInput = driver.findElement(By.id("capacidade"));
		capInput.clear();
		capInput.sendKeys(capacidadeSala);
		driver.findElement(By.xpath("//button[text()='Salvar']")).click();
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciamento de Salas"));

		// --- 3. AÇÃO: Editar a Sala e Associar o Equipamento ---
		String xpathBotaoEditar = String.format(
				"//tr[td[normalize-space()='%s']]//a[normalize-space()='Editar']",
				nomeSala
		);
		wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathBotaoEditar))).click();
		String xpathCheckboxLabel = String.format(
				"//label[normalize-space()='%s']",
				nomeEquipamento
		);
		WebElement checkboxLabel = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathCheckboxLabel)));
		checkboxLabel.click();
		driver.findElement(By.xpath("//button[text()='Salvar']")).click();
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciamento de Salas"));

		// --- 4. VERIFICAÇÃO: Checar na Página Inicial ---
		driver.get(baseUrl + "/");
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciador de Reservas de Salas"));
		String xpathVerificacaoFinal = String.format(
				"//div[@class='sala-detalhes'][h3[contains(text(), '%s')]]//ul/li[normalize-space()='%s']",
				nomeSala,
				nomeEquipamento
		);
		WebElement itemEquipamentoNaLista = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathVerificacaoFinal))
		);
		assertNotNull(itemEquipamentoNaLista, "O equipamento não foi associado corretamente à sala.");
	}

	@Test
	public void CT06_CadastrarNovaReservaComSucesso() {
		// --- 1. SETUP: Cadastrar uma Sala para a reserva ---
		String salaId = "R01";
		String nomeSala = "Sala para Reserva";
		String capacidadeSala = "8";

		driver.get(baseUrl + "/salas/nova");
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Nova Sala"));
		driver.findElement(By.id("id")).sendKeys(salaId);
		driver.findElement(By.id("nome")).sendKeys(nomeSala);
		WebElement capInput = driver.findElement(By.id("capacidade"));
		capInput.clear();
		capInput.sendKeys(capacidadeSala);
		driver.findElement(By.xpath("//button[text()='Salvar']")).click();
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciamento de Salas"));

		// --- 2. AÇÃO: Fazer a Reserva na Página Inicial ---
		String nomeResponsavel = "Maria";

		LocalDateTime agora = LocalDateTime.now();
		LocalDateTime inicioReserva = agora.plusHours(1).withMinute(0).withSecond(0);
		LocalDateTime fimReserva = inicioReserva.plusHours(2);
		DateTimeFormatter formatoInput = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
		String inicioInput = inicioReserva.format(formatoInput);
		String fimInput = fimReserva.format(formatoInput);
		DateTimeFormatter formatoTabela = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		String inicioTabela = inicioReserva.format(formatoTabela);
		String fimTabela = fimReserva.format(formatoTabela);

		driver.get(baseUrl + "/");
		wait.until(ExpectedConditions.textToBe(By.tagName("h1"), "Gerenciador de Reservas de Salas"));

		WebElement selectElement = driver.findElement(By.id("sala"));
		Select selectSala = new Select(selectElement);
		selectSala.selectByVisibleText(nomeSala);
		driver.findElement(By.id("responsavel")).sendKeys(nomeResponsavel);

		JavascriptExecutor js = (JavascriptExecutor) driver;
		WebElement inicioInputEl = driver.findElement(By.id("inicio"));
		WebElement fimInputEl = driver.findElement(By.id("fim"));
		js.executeScript("arguments[0].value = arguments[1];", inicioInputEl, inicioInput);
		js.executeScript("arguments[0].value = arguments[1];", fimInputEl, fimInput);
		driver.findElement(By.xpath("//button[text()='Fazer Reserva']")).click();

		// --- 3. VERIFICAÇÃO ---
		WebElement msgSucesso = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("sucesso")));
		assertTrue(msgSucesso.isDisplayed(), "A mensagem de sucesso da reserva não apareceu.");

		String xpathBusca = String.format(
				"//div[@class='lista-reservas']//tr[" +
						"td[normalize-space()='%s'] and " + // Responsável
						"td[normalize-space()='%s'] and " + // Nome da Sala
						"td[normalize-space()='%s'] and " + // Início
						"td[normalize-space()='%s']" +       // Fim
						"]",
				nomeResponsavel, nomeSala, inicioTabela, fimTabela
		);
		WebElement reservaNaTabela = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath(xpathBusca))
		);
		assertNotNull(reservaNaTabela, "A reserva cadastrada não foi encontrada na tabela.");
	}
}