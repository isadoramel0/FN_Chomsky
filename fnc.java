import java.io.*;
import java.util.*;

public class fnc {
    // converter de Map<String, List<String>> pra Map<String, StringBuilder>
    private static Map<String, StringBuilder> converterParaStringBuilder(Map<String, List<String>> regras) {
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
    private static Map<String, List<String>> converterProducoes(Map<String, StringBuilder> producoes) {
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

    private static List<String> gerarCombinacoes(String producao, String nula) {
        List<String> combinacoes = new ArrayList<>();
        int index = producao.indexOf(nula);
        while (index != -1) {
            String novaProducao = producao.substring(0, index) + producao.substring(index + 1);
            if (!novaProducao.isEmpty() && !combinacoes.contains(novaProducao)) {
                combinacoes.add(novaProducao);
            }
            index = producao.indexOf(nula, index + 1);
        }
        return combinacoes;
    }

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

        // Quando tem uma regra com todos sendo anuláveis e essa regra estiver no simbolo inicial, adicionar λ caso não tenha
        boolean todasNulas = true;
        for (String producao : regras.get(simboloInicial)) {
            for (char c : producao.toCharArray()) {
                if (!nulas.contains(String.valueOf(c))) {
                    todasNulas = false;
                    break;
                }
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

    public static void recSimbInicial(Map<String, StringBuilder> producoes, String simbInicial) {

        boolean precisaNovoSimbolo = false;

        // Verificar se há recursão direta no símbolo inicial
        for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
            String chave = entry.getKey();
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
            } 
            else {
                // Adicionar as produções não recursivas, substituindo o símbolo inicial pelo novo símbolo
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

    // Método para ler a gramática de glc1.txt
    private static Map<String, StringBuilder> leituraArq(String inputFile) throws IOException {
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
    private static void escritaArq(Map<String, StringBuilder> producoes, String outputFile, String simbInicial)
            throws IOException {
        try (BufferedWriter escrita = new BufferedWriter(new FileWriter(outputFile))) {

            String novoSimbolo = simbInicial + "'";
            if (producoes.containsKey(novoSimbolo)) {
                escrita.write(novoSimbolo + " -> " + producoes.get(novoSimbolo).toString());
                escrita.newLine();
            }

            // Escrever os demais símbolos, exceto o símbolo inicial e seu novo símbolo
            for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
                if (!entry.getKey().equals(novoSimbolo)) {
                    escrita.write(entry.getKey() + " -> " + entry.getValue().toString());
                    escrita.newLine();
                }
            }
        }
    }

    // Mostrar gramática em ordem alfabética dps do S' e S
    private static void mostrarGramatica(Map<String, StringBuilder> producoes) {
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
        naoTerminais.subList(2, naoTerminais.size()).sort(String::compareTo);
        
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

            // Escrever a gramática transformada no arquivo de saída
            escritaArq(producoes, outputFile, simbInicial);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
