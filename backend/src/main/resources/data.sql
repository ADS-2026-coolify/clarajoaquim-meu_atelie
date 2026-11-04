INSERT INTO canal_aquisicao (descricao) VALUES
  ('Indicacao de amigo/familiar'),
  ('Instagram'),
  ('Facebook'),
  ('Busca no Google'),
  ('Feira/Evento'),
  ('Vizinhanca'),
  ('Outro')
ON CONFLICT (descricao) DO NOTHING;
