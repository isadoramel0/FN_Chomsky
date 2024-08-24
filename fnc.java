import java.io.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class fnc {
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
        String novoSimbolo = simbInicial + "'";
        StringBuilder novasProducoes = new StringBuilder();
        StringBuilder producoesNaoRecursivas = new StringBuilder();

        // Processar as produções
        for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
            String chave = entry.getKey();
            StringBuilder regra = entry.getValue();
            String regraStr = regra.toString().trim();
            
            if (chave.equals(simbInicial)) {
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
    private static void escritaArq(Map<String, StringBuilder> producoes, String outputFile, String simbInicial) throws IOException {
        try (BufferedWriter escrita = new BufferedWriter(new FileWriter(outputFile))) {
            // Primeiro escrever o símbolo inicial e o seu novo símbolo imediatamente após
            escrita.write(simbInicial + " -> " + producoes.get(simbInicial).toString());
            escrita.newLine();

            String novoSimbolo = simbInicial + "'";
            if (producoes.containsKey(novoSimbolo)) {
                escrita.write(novoSimbolo + " -> " + producoes.get(novoSimbolo).toString());
                escrita.newLine();
            }

            // Escrever os demais símbolos, exceto o símbolo inicial e seu novo símbolo
            for (Map.Entry<String, StringBuilder> entry : producoes.entrySet()) {
                if (!entry.getKey().equals(simbInicial) && !entry.getKey().equals(novoSimbolo)) {
                    escrita.write(entry.getKey() + " -> " + entry.getValue().toString());
                    escrita.newLine();
                }
            }
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

            // Identificar o símbolo inicial (primeira chave lida no mapa)
            String simbInicial = producoes.keySet().iterator().next();

            // Remover a recursão do símbolo inicial se houver
            recSimbInicial(producoes, simbInicial);

            // Escrever a gramática transformada no arquivo de saída
            escritaArq(producoes, outputFile, simbInicial);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
