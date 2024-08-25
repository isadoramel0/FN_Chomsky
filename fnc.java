
import java.io.*;
import java.util.*;

public class fnc {
    // converter de Map<String, List<String>> pra Map<String, StringBuilder>
    public static Map<String, StringBuilder> converterParaStringBuilder(Map<String, List<String>> regras) {
        Map<String, StringBuilder> producoes = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : regras.entrySet()) {
            String naoTerminal = entry.getKey();
            List<String> listaRegras = entry.getValue();

            StringBuilder producaoStringBuilder = new StringBuilder();

            for (int i = 0; i < listaRegras.size(); i++) {
                producaoStringBuilder.append(listaRegras.get(i));
                if (i < listaRegras.size() - 1) {
                    producaoStringBuilder.append(" | ");
                }
            }

            producoes.put(naoTerminal, producaoStringBuilder);
        }

        return producoes;
    }

    // converter de Map<String, StringBuilder> pra Map<String, List<String>>
    public static Map<String, List<String>> converterProducoes(Map<String, StringBuilder> producoes) {
        Map<String, List<String>> regras = new HashMap<>();

        for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
            String naoTerminal = entry.getKey();
            StringBuilder producaoStringBuilder = entry.getValue();
            String[] regrasArray = producaoStringBuilder.toString().split("\\|");
            List<String> listaRegras = new ArrayList<>(Arrays.asList(regrasArray));

            listaRegras.replaceAll(String::trim);

            regras.put(naoTerminal, listaRegras);
        }

        return regras;
    }

    // 1° Passo: remover recursividade do símbolo inicial
    public static void recSimbInicial(Map<String, StringBuilder> producoes, String simbInicial) {

        boolean precisaNovoSimbolo = false;

        // Verificar se há recursão direta no símbolo inicial
        for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
            // String chave = entry.getKey();
            StringBuilder regras = entry.getValue();

            // Verificar se a produção contém o símbolo inicial
            if (regras.toString().contains(simbInicial)) {
                precisaNovoSimbolo = true;
                break;
            }
        }

        if (!precisaNovoSimbolo) {
            // Se não há recursão, não precisa criar um novo símbolo
            return;
        }

        // Criar um novo símbolo para lidar com a recursão
        String novoSimbolo = simbInicial;
        simbInicial = simbInicial + "'";
        StringBuilder novasProducoes = new StringBuilder();
        StringBuilder producoesNaoRecursivas = new StringBuilder();

        // Processar as produções
        for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
            String chave = entry.getKey();
            StringBuilder regra = entry.getValue();
            String regraStr = regra.toString().trim();

            if (chave.equals(novoSimbolo)) {
                // Copiar todas as produções do símbolo inicial para o novo símbolo
                String[] partes = regraStr.split("\\|");
                for (String parte : partes) {
                    String parteTrimmed = parte.trim();
                    // Adicionar produções do símbolo inicial ao novo símbolo
                    if (novasProducoes.length() > 0) {
                        novasProducoes.append(" | ");
                    }
                    novasProducoes.append(parteTrimmed);
                }
            } else {
                // Adicionar as produções não recursivas, substituindo o símbolo inicial pelo
                // novo símbolo
                String regraAtualizada = regraStr.replace(simbInicial, novoSimbolo);
                if (producoesNaoRecursivas.length() > 0) {
                    producoesNaoRecursivas.append(" | ");
                }
                producoesNaoRecursivas.append(regraAtualizada);
            }
        }

        // Atualizar a regra do símbolo inicial para apontar somente para o novo símbolo
        producoes.put(simbInicial, new StringBuilder(novoSimbolo));
        // Adicionar o novo símbolo ao mapa de produções
        producoes.put(novoSimbolo, novasProducoes.length() > 0 ? novasProducoes : new StringBuilder("."));

        for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
            String chave = entry.getKey();
            StringBuilder regra = entry.getValue();
            if (!chave.equals(simbInicial)) {
                String regraAtualizada = regra.toString().replace(simbInicial, novoSimbolo);
                producoes.put(chave, new StringBuilder(regraAtualizada));
            }
        }
    }

    public static List<String> gerarCombinacoes(String producao, String nula) {
        List<String> combinacoes = new ArrayList<>();
    Queue<String> queue = new LinkedList<>();
    queue.add(producao);

    while (!queue.isEmpty()) {
        String current = queue.poll();
        int index = current.indexOf(nula);

        // Adiciona a produção atual se não estiver vazia e ainda não estiver na lista de combinações
        if (!current.isEmpty() && !combinacoes.contains(current)) {
            combinacoes.add(current);
        }

        // Enfileira todas as produções geradas removendo uma ocorrência de 'nula'
        while (index != -1) {
            String novaProducao = current.substring(0, index) + current.substring(index + 1);
            if (!combinacoes.contains(novaProducao)) {
                queue.add(novaProducao);
            }
            index = current.indexOf(nula, index + 1);
        }
    }

    return combinacoes;

    }

    // 2° Passo: remover regras λ
    public static Map<String, StringBuilder> removerRegrasNulas(Map<String, StringBuilder> producoes) {
        Map<String, List<String>> regras = converterProducoes(producoes);
        Set<String> nulas = new HashSet<>();

        // Encontrar todas as variáveis que produzem λ diretamente
        for (String variavel : regras.keySet()) {
            if (regras.get(variavel).contains(".")) {
                nulas.add(variavel);
            }
        }

        // Encontrar variáveis que produzem λ indiretamente
        Set<String> prevNulas;
        do {
            prevNulas = new HashSet<>(nulas);

            for (String variavel : regras.keySet()) {
                for (String producao : regras.get(variavel)) {
                    boolean todasNulas = true;
                    for (char c : producao.toCharArray()) {
                        if (!nulas.contains(String.valueOf(c))) {
                            todasNulas = false;
                            break;
                        }
                    }
                    if (todasNulas) {
                        nulas.add(variavel);
                    }
                }
            }
        } while (!nulas.equals(prevNulas));

        String simboloInicial = producoes.containsKey("S'") ? "S'" : "S";
        List<String> novasProducoesSimboloInicial = new ArrayList<>();
        Set<String> producoesExistentes = new HashSet<>(regras.get(simboloInicial));

        // Quando tem uma regra com todos sendo anuláveis e essa regra estiver no
        // simbolo inicial, adicionar λ caso não tenha
        boolean todasNulas = false;
        for (String producao : regras.get(simboloInicial)) {
            System.out.println(producao);
            boolean producaoTodaNula = true;

            for (char c : producao.toCharArray()) {
                if (!nulas.contains(String.valueOf(c))) {
                    producaoTodaNula = false;
                    break;
                }
            }

            if (producaoTodaNula) {
                todasNulas = true;
                break;
            }
        }

        if (todasNulas && !regras.get(simboloInicial).contains(".")) {
            novasProducoesSimboloInicial.add(".");
        }

        // Gerar novas combinações a partir dos q são anuláveis
        for (String nula : nulas) {
            for (String variavel : regras.keySet()) {
                List<String> novasProducoes = new ArrayList<>();
                producoesExistentes = new HashSet<>(regras.get(variavel));

                for (String producao : regras.get(variavel)) {
                    if (producao.contains(nula)) {
                        List<String> combinacoes = gerarCombinacoes(producao, nula);
                        for (String combinacao : combinacoes) {
                            if (!producoesExistentes.contains(combinacao)) {
                                novasProducoes.add(combinacao);
                                producoesExistentes.add(combinacao);
                            }
                        }
                    }
                }
                regras.get(variavel).addAll(novasProducoes);
            }
        }

        // Remover todas as produções λ finais
        for (String variavel : regras.keySet()) {
            regras.get(variavel).remove(".");
        }

        // Adicionar as novas produções λ ao símbolo inicial se necessário
        regras.get(simboloInicial).addAll(novasProducoesSimboloInicial);

        Map<String, StringBuilder> producoesAtualizada = converterParaStringBuilder(regras);
        return producoesAtualizada;
    }

    // 3° Passo: remover regras de cadeia
    public static Map<String, StringBuilder> removeRegraDeCadeia(Map<String, StringBuilder> producoes) {
        // Converter as produções para o formato List<String> para facilitar o
        // processamento
        Map<String, List<String>> regras = converterProducoes(producoes);

        // Calcular chain(V) para cada variável V
        Map<String, Set<String>> chainSets = new HashMap<>();
        for (String variavel : regras.keySet()) {
            chainSets.put(variavel, calcularChain(variavel, regras));
        }

        // Substituir regras de cadeia pelas produções diretas
        Map<String, List<String>> novasRegras = new HashMap<>();
        for (String variavel : regras.keySet()) {
            Set<String> chainSet = chainSets.get(variavel);
            List<String> novasProducoes = new ArrayList<>();

            for (String w : chainSet) {
                List<String> producoesW = regras.get(w);
                for (String producao : producoesW) {
                    // Ignorar regras de cadeia (do tipo W -> X)
                    if (!regras.containsKey(producao.trim())) {
                        if (!novasProducoes.contains(producao)) {
                            novasProducoes.add(producao.trim());
                        }
                    }
                }
            }

            novasRegras.put(variavel, novasProducoes);
        }

        // Converter as novas regras de volta para o formato Map<String, StringBuilder>
        Map<String, StringBuilder> producoesAtualizadas = converterParaStringBuilder(novasRegras);

        return producoesAtualizadas;
    }

    // Função auxiliar para calcular o conjunto chain(V)
    public static Set<String> calcularChain(String variavel, Map<String, List<String>> regras) {
        Set<String> chainSet = new HashSet<>();
        Queue<String> fila = new LinkedList<>();
        fila.add(variavel);

        while (!fila.isEmpty()) {
            String atual = fila.poll();
            if (!chainSet.contains(atual)) {
                chainSet.add(atual);
                for (String producao : regras.get(atual)) {
                    producao = producao.trim();
                    if (regras.containsKey(producao)) { // Se a produção é uma variável
                        fila.add(producao);
                    }
                }
            }
        }

        return chainSet;
    }

    // 4° Passo: remover variáveis inúteis
    public static Map<String, StringBuilder> term(Map<String, StringBuilder> producoes) {
        // Converter as produções para o formato List<String>
        Map<String, List<String>> regras = converterProducoes(producoes);

        // Etapa 1: Identificar as variáveis que podem gerar cadeias terminais
        Set<String> geradoras = new HashSet<>();
        boolean mudou;

        do {
            mudou = false;
            for (Map.Entry<String, List<String>> entry : regras.entrySet()) {
                String variavel = entry.getKey();
                List<String> producoesVariavel = entry.getValue();

                if (!geradoras.contains(variavel)) {
                    for (String producao : producoesVariavel) {
                        boolean geraTerminais = true;
                        for (char c : producao.toCharArray()) {
                            String s = String.valueOf(c);
                            if (Character.isUpperCase(c) && !geradoras.contains(s)) {
                                geraTerminais = false;
                                break;
                            }
                        }
                        if (geraTerminais) {
                            geradoras.add(variavel);
                            mudou = true;
                            break;
                        }
                    }
                }
            }
        } while (mudou);

        // Etapa 2: Remover produções e variáveis que não são geradoras
        Map<String, List<String>> novasRegras = new HashMap<>();
        for (String variavel : geradoras) {
            List<String> novasProducoes = new ArrayList<>();
            for (String producao : regras.get(variavel)) {
                boolean somenteGeradoras = true;
                for (char c : producao.toCharArray()) {
                    String s = String.valueOf(c);
                    if (Character.isUpperCase(c) && !geradoras.contains(s)) {
                        somenteGeradoras = false;
                        break;
                    }
                }
                if (somenteGeradoras) {
                    novasProducoes.add(producao);
                }
            }
            if (!novasProducoes.isEmpty()) {
                novasRegras.put(variavel, novasProducoes);
            }
        }

        // Converter as novas regras de volta para o formato Map<String, StringBuilder>
        Map<String, StringBuilder> producoesAtualizadas = converterParaStringBuilder(novasRegras);
        return producoesAtualizadas;
    }

    public static void reach(Map<String, StringBuilder> producoes, String simboloInicial) throws IOException {
        Set<String> acessiveis = new HashSet<>();
        acessiveis.add(simboloInicial); // Adiciona o símbolo inicial

        boolean mudou = true;

        try {
            while (mudou) {
                mudou = false;

                // Iterar por todas as produções
                for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
                    String simbolo = entry.getKey();
                    String regra = entry.getValue().toString().trim();

                    if (acessiveis.contains(simbolo)) {
                        String[] producoesRegra = regra.split("\\|");

                        // Verificar cada parte da produção
                        for (String producao : producoesRegra) {
                            for (char c : producao.toCharArray()) {
                                String simboloNovo = String.valueOf(c);

                                // Se for um não-terminal e ainda não estiver no conjunto
                                if (Character.isUpperCase(c) && !acessiveis.contains(simboloNovo)) {
                                    acessiveis.add(simboloNovo);
                                    mudou = true; // Houve mudança, precisa repetir
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // Remover as produções que não são acessíveis
        producoes.keySet().removeIf(simbolo -> !acessiveis.contains(simbolo));
    }

    public static Map<String, StringBuilder> colocarNaFNC(Map<String, StringBuilder> producoesOriginal,
            String outputfile,
            String simbInicial) {
        Map<String, List<String>> regras = converterProducoes(producoesOriginal);
        Map<String, List<List<String>>> novasProducoesMap = new HashMap<>();

        // Colocar produções como sendo um vetor, em que cada LETRA é uma posição desse
        // vetor
        for (String variavel : regras.keySet()) {
            List<List<String>> novasProducoes = new ArrayList<>();

            for (String producao : regras.get(variavel)) {
                // Converter a produção em uma lista de strings, onde cada string é uma letra
                List<String> novaProducao = new ArrayList<>();
                for (char c : producao.toCharArray()) {
                    novaProducao.add(String.valueOf(c));
                }
                novasProducoes.add(novaProducao);
            }

            // Adicionar as novas produções ao mapa de novas regras
            novasProducoesMap.put(variavel, novasProducoes);
        }

        // Ajustando de forma que não tenham terminais em produções de tamanho maior que
        // 1 (A -> b)
        Map<String, String> terminaisSubstituidos = new HashMap<>();
        Map<String, List<List<String>>> producoesAtualizadasMap = new HashMap<>();
        for (String variavel : novasProducoesMap.keySet()) {
            List<List<String>> producoes = novasProducoesMap.get(variavel);
            List<List<String>> novasProducoes = new ArrayList<>();
            for (List<String> producao : producoes) {
                List<String> novaProducao = new ArrayList<>();
                for (String simbolo : producao) {
                    if (simbolo.length() == 1 && Character.isLowerCase(simbolo.charAt(0)) && producao.size() > 1) {
                        if (!terminaisSubstituidos.containsKey(simbolo)) {
                            // Criar uma nova variável para o terminal
                            String novaVariavel = Character.toUpperCase(simbolo.charAt(0)) + "'";
                            terminaisSubstituidos.put(simbolo, novaVariavel);
                            producoesAtualizadasMap.put(novaVariavel,
                                    Collections.singletonList(Collections.singletonList(simbolo)));
                        }
                        novaProducao.add(terminaisSubstituidos.get(simbolo));
                    } else {
                        novaProducao.add(simbolo);
                    }
                }
                novasProducoes.add(novaProducao);
            }
            producoesAtualizadasMap.put(variavel, novasProducoes);
        }

        // Criando novas regras para que não tenham produções de tamanho maior que 2 (A
        // -> BC)
        Map<String, List<List<String>>> regrasTemporarias = new HashMap<>();
        Map<String, List<String>> producoesTemporariasMap = new HashMap<>();
        int contadorTemporario = 1;

        for (String variavel : producoesAtualizadasMap.keySet()) {
            List<List<String>> producoes = producoesAtualizadasMap.get(variavel);
            List<List<String>> novasProducoes = new ArrayList<>();

            for (List<String> producao : producoes) {
                if (producao.size() > 2) {
                    List<String> parteAtual = new ArrayList<>(producao);

                    while (parteAtual.size() > 2) {
                        // Pega as duas últimas posições
                        List<String> subParte = parteAtual.subList(parteAtual.size() - 2, parteAtual.size());

                        // Atualiza a parte atual removendo as duas últimas posições
                        parteAtual = parteAtual.subList(0, parteAtual.size() - 2);

                        // Verifica se a regra temporária já existe
                        String regraRepetida = null;
                        boolean possui = false;
                        for (Map.Entry<String, List<String>> entry : producoesTemporariasMap.entrySet()) {
                            if (entry.getValue().equals(subParte)) {
                                possui = true;
                                regraRepetida = entry.getKey();
                                break;
                            }
                        }

                        String novaVariavel;
                        if (!possui) {
                            novaVariavel = "T" + contadorTemporario++;
                            List<String> novaParte = new ArrayList<>(subParte);
                            producoesTemporariasMap.put(novaVariavel, novaParte);
                            List<List<String>> producaoTemporaria = Collections.singletonList(novaParte);
                            regrasTemporarias.put(novaVariavel, producaoTemporaria);
                        } else {
                            novaVariavel = regraRepetida; // Reutiliza a regra existente
                        }

                        // Adiciona a nova variável temporária na produção
                        parteAtual.add(novaVariavel);
                    }
                    novasProducoes.add(parteAtual);
                } else {
                    novasProducoes.add(producao);
                }
            }
            producoesAtualizadasMap.put(variavel, novasProducoes);
        }
        // Adicionar as regras temporárias ao mapa de produções atualizadas
        producoesAtualizadasMap.putAll(regrasTemporarias);

        Map<String, StringBuilder> prods = new LinkedHashMap<>();

        // Processar cada entrada no mapa original
        for (Map.Entry<String, List<List<String>>> entry : producoesAtualizadasMap.entrySet()) {
            String variavel = entry.getKey();
            List<List<String>> producoesList = entry.getValue();
            StringBuilder sb = new StringBuilder();

            // Adicionar produções ao StringBuilder
            for (int i = 0; i < producoesList.size(); i++) {
                if (i > 0) {
                    sb.append(" | ");
                }
                List<String> producao = producoesList.get(i);
                sb.append(String.join("", producao));
            }

            // Adicionar a entrada ao mapa transformado
            prods.put(variavel, sb);
        }

        return prods;
    }

    // Método para ler a gramática de glc1.txt
    public static Map<String, StringBuilder> leituraArq(String inputFile) throws IOException {
        // Usamos LinkedHashMap para garantir a ordem de inserção
        Map<String, StringBuilder> producoes = new LinkedHashMap<>();
        try (BufferedReader leitura = new BufferedReader(new FileReader(inputFile))) {
            String linha;
            String simbolo = null;
            while ((linha = leitura.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) {
                    continue;
                }

                if (linha.contains("->")) {
                    int separacao = linha.indexOf("->");
                    simbolo = linha.substring(0, separacao).trim();
                    String producoesLinha = linha.substring(separacao + 2).trim();
                    String[] producoesSep = producoesLinha.split("\\|");
                    StringBuilder sb = producoes.getOrDefault(simbolo, new StringBuilder());
                    for (String prod : producoesSep) {
                        String trimmedProd = prod.trim();
                        if (sb.length() > 0) {
                            sb.append(" | ");
                        }
                        sb.append(trimmedProd);
                    }
                    producoes.put(simbolo, sb);
                }
            }
        }
        return producoes;
    }

    // Método para escrever em glc1_fnc.txt
    public static void escritaArq(Map<String, StringBuilder> producoes, String outputFile, String simbInicial)
            throws IOException {
        try (BufferedWriter escrita = new BufferedWriter(new FileWriter(outputFile))) {
            List<String> naoTerminais = new ArrayList<>();

            // Adicionar 'S'' primeiro, se existir
            if (producoes.containsKey("S'")) {
                naoTerminais.add("S'");
            }

            // Adicionar 'S' em seguida, se existir
            if (producoes.containsKey("S")) {
                naoTerminais.add("S");
            }

            // Adicionar os demais não terminais
            for (String naoTerminal : producoes.keySet()) {
                if (!naoTerminal.equals("S'") && !naoTerminal.equals("S")) {
                    naoTerminais.add(naoTerminal);
                }
            }

            // Ordenar os demais não terminais em ordem alfabética, exceto os S
            if (naoTerminais.size() > 2) {
                naoTerminais.subList(2, naoTerminais.size()).sort(String::compareTo);
            }

            // Escrever as produções na ordem correta
            for (String naoTerminal : naoTerminais) {
                escrita.write(naoTerminal + " -> " + producoes.get(naoTerminal).toString());
                escrita.newLine();
            }
        } catch (IOException e) {
            System.err.println("Erro ao escrever no arquivo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Mostrar gramática em ordem alfabética dps do S' e S
    public static void mostrarGramatica(Map<String, StringBuilder> producoes) {
        List<String> naoTerminais = new ArrayList<>();
        List<String> alfabetoSemNumerosEAspas = new ArrayList<>();
        List<String> letraTComNumero = new ArrayList<>();
        List<String> alfabetoComAspas = new ArrayList<>();

        // Adicionar 'S'' primeiro, se existir
        if (producoes.containsKey("S'")) {
            naoTerminais.add("S'");
        }

        // Adicionar 'S' em seguida, se existir
        if (producoes.containsKey("S")) {
            naoTerminais.add("S");
        }

        // Categorizar os demais não terminais
        for (String naoTerminal : producoes.keySet()) {
            if (!naoTerminal.equals("S'") && !naoTerminal.equals("S")) {
                if (naoTerminal.matches("^[A-RU-Z]$")) {
                    alfabetoSemNumerosEAspas.add(naoTerminal);
                } else if (naoTerminal.matches("^T\\d+$")) {
                    letraTComNumero.add(naoTerminal);
                } else if (naoTerminal.matches("^[A-Z]'$")) {
                    alfabetoComAspas.add(naoTerminal);
                }
            }
        }

        // Ordenar cada lista
        alfabetoSemNumerosEAspas.sort(String::compareTo);
        letraTComNumero.sort(String::compareTo);
        alfabetoComAspas.sort(String::compareTo);

        // Adicionar todos na ordem correta
        naoTerminais.addAll(alfabetoSemNumerosEAspas);
        naoTerminais.addAll(letraTComNumero);
        naoTerminais.addAll(alfabetoComAspas);

        // Mostrar a gramática na ordem correta
        for (String naoTerminal : naoTerminais) {
            String producao = producoes.get(naoTerminal).toString();
            System.out.println(naoTerminal + " -> " + producao);
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Use: java fnc.java <glc1.txt> <glc1_fnc.txt>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            // Ler a gramática do arquivo de entrada
            Map<String, StringBuilder> producoes = leituraArq(inputFile);
            mostrarGramatica(producoes);

            // Identificar o símbolo inicial (primeira chave lida no mapa)
            String simbInicial = producoes.keySet().iterator().next();

            // Remover a recursão do símbolo inicial se houver
            System.out.println("Retirar recursao no simbolo inicial: ");
            recSimbInicial(producoes, simbInicial);
            mostrarGramatica(producoes);

            // Remover regras nulas
            System.out.println("Remover regras nulas: ");
            producoes = removerRegrasNulas(producoes);
            mostrarGramatica(producoes);

            // Remover regras de cadeia
            System.out.println("Remover regras de cadeia: ");
            producoes = removeRegraDeCadeia(producoes);
            mostrarGramatica(producoes);

            // Remover variáveis inúteis
            System.out.println("Remover símbolos inuteis: ");
            producoes = term(producoes);
            simbInicial = producoes.containsKey("S'") ? "S'" : "S";
            reach(producoes, simbInicial);
            mostrarGramatica(producoes);

            // Colocar na FNC
            System.out.println("Colocar na FNC: ");
            producoes = colocarNaFNC(producoes, outputFile, simbInicial);
            mostrarGramatica(producoes);

            escritaArq(producoes, outputFile, simbInicial);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}